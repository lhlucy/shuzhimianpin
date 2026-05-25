package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lingshu.config.EmailConfig;
import com.lingshu.entity.VerificationCode;
import com.lingshu.mapper.VerificationCodeMapper;
import com.lingshu.util.IpUtil;
import com.lingshu.util.RandomUtil;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;
    private final VerificationCodeMapper verificationCodeMapper;

    @Value("${app.verification.email-expire-minutes:10}")
    private int emailExpireMinutes;

    @Value("${app.verification.email-resend-seconds:60}")
    private int emailResendSeconds;

    @Value("${app.verification.max-attempts-per-ip:10}")
    private int maxAttemptsPerIp;

    @Transactional
    public void sendVerificationCode(String email, VerificationCode.CodeType type,
                                     HttpServletRequest request) {

        String ip = IpUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // 检查发送频率
        checkSendFrequency(ip);

        // 检查冷却时间
        checkCooldown(email, type);

        // 生成6位验证码
        String code = RandomUtil.generateDigitCode(6);

        // 保存验证码记录
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setIp(ip);
        verificationCode.setUserAgent(userAgent);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(emailExpireMinutes));

        verificationCodeMapper.insert(verificationCode);

        // 发送邮件
        sendEmailInternal(email, code, type);

        log.info("验证码发送: {} -> {}, type: {}", email, code, type);
    }

    public boolean validateCode(String email, String code, VerificationCode.CodeType type) {
        QueryWrapper<VerificationCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email)
                .eq("type", type)
                .eq("used", false)
                .gt("expire_time", LocalDateTime.now())
                .orderByDesc("create_time")
                .last("LIMIT 1");

        VerificationCode verificationCode = verificationCodeMapper.selectOne(queryWrapper);

        if (verificationCode == null) {
            return false;
        }

        if (!verificationCode.getCode().equals(code)) {
            return false;
        }

        // 标记验证码为已使用
        verificationCode.setUsed(true);
        verificationCodeMapper.updateById(verificationCode);

        return true;
    }

    /**
     * 内部发送邮件方法
     */
    private void sendEmailInternal(String toEmail, String code, VerificationCode.CodeType type) {
        // 检查是否开启mock模式
        if (emailConfig.getMock()) {
            log.info("模拟发送验证码 {}: 验证码 = {}, 类型 = {}", toEmail, code, type);
            return;
        }

        // 检查是否使用SendGrid
        if (emailConfig.getProvider() != null &&
                "sendgrid".equals(emailConfig.getProvider())) {
            sendViaSendGrid(toEmail, code, type);
            return;
        }

        // 默认使用SMTP发送（如QQ邮箱）
        sendViaSmtp(toEmail, code, type);
    }

    /**
     * SMTP发送邮件（如QQ邮箱）
     */
    private void sendViaSmtp(String toEmail, String code, VerificationCode.CodeType type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置发件人（必须与配置的一致）
            helper.setFrom(emailConfig.getFrom());

            helper.setTo(toEmail);

            String subject = getSubjectByType(type);
            helper.setSubject(subject);

            String operation = getOperationByType(type);
            String content = emailConfig.getCodeTemplate()
                    .replace("{operation}", operation)
                    .replace("{code}", code);

            helper.setText(content, true);

            mailSender.send(message);
            log.info("SMTP邮件发送: {}", toEmail);

        } catch (Exception e) {  // 捕获所有异常
            log.error("SMTP邮件发送失败", e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * SendGrid发送邮件
     */
    private void sendViaSendGrid(String toEmail, String code, VerificationCode.CodeType type) {
        try {
            // 检查API Key
            if (emailConfig.getSendgridApiKey() == null || emailConfig.getSendgridApiKey().isEmpty()) {
                throw new RuntimeException("SendGrid API Key未配置");
            }

            // SendGrid客户端
            SendGrid sg = new SendGrid(emailConfig.getSendgridApiKey());
            Email from = new Email(emailConfig.getFrom(), emailConfig.getFromName());
            Email to = new Email(toEmail);

            String subject = getSubjectByType(type);
            String operation = getOperationByType(type);

            String htmlContent = emailConfig.getCodeTemplate()
                    .replace("{operation}", operation)
                    .replace("{code}", code);

            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("SendGrid邮件发送: {}, 状态码: {}", toEmail, response.getStatusCode());
            } else {
                log.error("SendGrid邮件发送失败: {}, 状态码: {}, 响应: {}",
                        toEmail, response.getStatusCode(), response.getBody());
                throw new RuntimeException("邮件发送失败，状态码: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("SendGrid邮件发送异常", e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取邮件主题
     */
    private String getSubjectByType(VerificationCode.CodeType type) {
        return switch (type) {
            case LOGIN -> emailConfig.getLoginSubject();
            case REGISTER -> emailConfig.getRegisterSubject();
            case RESET_PASSWORD -> emailConfig.getResetPasswordSubject();
            case ACCOUNT_DELETE -> emailConfig.getDeleteAccountSubject();
            default -> "验证码 - 领书";
        };
    }

    /**
     * 根据类型获取操作描述
     */
    private String getOperationByType(VerificationCode.CodeType type) {
        return switch (type) {
            case LOGIN -> "登录";
            case REGISTER -> "注册";
            case RESET_PASSWORD -> "重置密码";
            case ACCOUNT_DELETE -> "删除账号";
            default -> "操作";
        };
    }

    private void checkSendFrequency(String ip) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        QueryWrapper<VerificationCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip", ip)
                .gt("create_time", oneHourAgo);
        Long count = verificationCodeMapper.selectCount(queryWrapper);

        if (count != null && count >= maxAttemptsPerIp) {
            throw new RuntimeException("发送频率过高，请稍后再试");
        }
    }

    private void checkCooldown(String email, VerificationCode.CodeType type) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusSeconds(emailResendSeconds);

        QueryWrapper<VerificationCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email)
                .eq("type", type)
                .eq("used", false)
                .gt("expire_time", oneMinuteAgo)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        VerificationCode recentCode = verificationCodeMapper.selectOne(queryWrapper);

        if (recentCode != null) {
            throw new RuntimeException("验证码已发送，请稍后再试");
        }
    }
}
