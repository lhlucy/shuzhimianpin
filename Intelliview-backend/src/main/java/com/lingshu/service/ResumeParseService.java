package com.lingshu.service;

import com.lingshu.dto.response.ResumeParseResult;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeParseService {

    private static final int MAX_RESUME_CONTENT_LENGTH = 12000;
    private static final Pattern SALARY_PATTERN = Pattern.compile("(面议|\\d{1,3}\\s*(?:-|~|至)\\s*\\d{1,3}\\s*(?:k|K|万|千)?|\\d{1,3}\\s*(?:k|K|万|千))");
    private static final List<String> CITY_KEYWORDS = Arrays.asList(
            "北京", "上海", "广州", "深圳", "杭州", "南京", "苏州", "成都", "武汉", "西安", "重庆", "长沙", "郑州", "天津", "合肥", "厦门", "青岛", "宁波"
    );
    private static final List<String> JOB_KEYWORDS = Arrays.asList(
            "Java后端", "后端开发", "前端开发", "Web前端", "Python开发", "算法工程师", "测试工程师", "产品经理", "数据分析", "大数据开发", "运维工程师", "全栈开发"
    );

    public ResumeParseResult parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "请先上传简历文件");
        }
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String content = normalizeResumeContent(extractText(fileName, file));
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "未能识别出简历内容，请更换为 PDF、DOCX、TXT 或 MD 文件");
        }

        return ResumeParseResult.builder()
                .content(content)
                .summary(buildDigest(content))
                .intentionJob(extractField(content, Arrays.asList("意向岗位", "目标岗位", "求职意向", "应聘岗位", "期望岗位"), JOB_KEYWORDS))
                .recruitmentType(extractRecruitmentType(content))
                .intentionCity(extractField(content, Arrays.asList("意向城市", "期望城市", "工作地点", "目标城市", "期望地点"), CITY_KEYWORDS))
                .expectedSalary(extractSalary(content))
                .contentLength(content.length())
                .build();
    }

    public String normalizeResumeContent(String resumeContent) {
        if (!StringUtils.hasText(resumeContent)) {
            return "";
        }
        String normalized = resumeContent.replace("\u0000", "")
                .replace("\r", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();
        return normalized.length() <= MAX_RESUME_CONTENT_LENGTH
                ? normalized
                : normalized.substring(0, MAX_RESUME_CONTENT_LENGTH);
    }

    public String buildDigest(String resumeContent) {
        if (!StringUtils.hasText(resumeContent)) {
            return "";
        }
        String digest = Arrays.stream(resumeContent.split("\\n"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .filter(line -> line.length() > 3)
                .limit(8)
                .collect(Collectors.joining("；"));
        return digest.length() <= 320 ? digest : digest.substring(0, 320);
    }

    private String extractText(String fileName, MultipartFile file) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        try {
            if (lower.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(file.getInputStream())) {
                    return new PDFTextStripper().getText(document);
                }
            }
            if (lower.endsWith(".docx")) {
                try (XWPFDocument document = new XWPFDocument(file.getInputStream());
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return extractor.getText();
                }
            }
            if (lower.endsWith(".txt") || lower.endsWith(".md")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "简历解析失败，请确认文件内容没有损坏");
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "目前仅支持 PDF、DOCX、TXT、MD 格式的简历");
    }

    private String extractField(String content, List<String> labels, List<String> fallbackKeywords) {
        for (String line : content.split("\\n")) {
            String compact = line.trim();
            if (!StringUtils.hasText(compact)) {
                continue;
            }
            for (String label : labels) {
                if (compact.contains(label)) {
                    String value = compact.replaceFirst(".*?" + Pattern.quote(label) + "\\s*[:：]?\\s*", "")
                            .replaceAll("[，,；;|/].*$", "")
                            .trim();
                    if (StringUtils.hasText(value) && value.length() <= 40) {
                        return value;
                    }
                }
            }
        }
        return fallbackKeywords.stream().filter(content::contains).findFirst().orElse("");
    }

    private String extractRecruitmentType(String content) {
        if (content.contains("实习") || content.contains("实习生")) {
            return "实习";
        }
        if (content.contains("校招") || content.contains("校园招聘") || content.contains("应届")) {
            return "校招";
        }
        if (content.contains("社招") || content.contains("社会招聘") || content.contains("工作经验")) {
            return "社招";
        }
        return "";
    }

    private String extractSalary(String content) {
        for (String line : content.split("\\n")) {
            if (line.contains("薪资") || line.contains("期望") || line.contains("待遇")) {
                Matcher matcher = SALARY_PATTERN.matcher(line);
                if (matcher.find()) {
                    return matcher.group(1).replaceAll("\\s+", "");
                }
            }
        }
        Matcher matcher = SALARY_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1).replaceAll("\\s+", "") : "";
    }
}
