// controller/admin/AdminQuestionController.java
package com.lingshu.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingshu.dto.request.QuestionCreateRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.QuestionDetailResponse;
import com.lingshu.dto.response.QuestionResponse;
import com.lingshu.dto.response.QuestionSimpleResponse;
import com.lingshu.entity.Question;
import com.lingshu.service.BankService;
import com.lingshu.service.admin.AdminQuestionService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lingshu.service.admin.ExcelImportExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminQuestionController {

    private final AdminQuestionService questionService;
    private final ExcelImportExportService excelImportExportService;
    private final SecurityUtil securityUtil;
    private final BankService bankService;

    /**
     * 创建面试题 - 管理员操作
     */
    @PostMapping("/v2")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> createQuestionV2(
            @Valid @RequestBody QuestionCreateRequest request) {

        Long userId = securityUtil.getCurrentUserId();
        QuestionDetailResponse question = questionService.createQuestion(request, userId);
        return ResponseEntity.ok(ApiResponse.success("创建成功", question));
    }

    /**
     * 更新面试题 - 管理员操作
     */
    @PutMapping("/v2/{id}")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> updateQuestionV2(
            @PathVariable Long id,
            @Valid @RequestBody QuestionCreateRequest request) {

        Long userId = securityUtil.getCurrentUserId();
        QuestionDetailResponse question = questionService.updateQuestion(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("更新成功", question));
    }

    /**
     * 获取面试题详情 - 管理员操作
     */
    @GetMapping("/v2/{id}")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> getQuestionDetail(@PathVariable Long id) {
        QuestionDetailResponse question = questionService.getQuestionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(question));
    }

    /**
     * 搜索问题 - 管理员操作
     */
    @GetMapping("/search/suggest")
    public ResponseEntity<ApiResponse<List<QuestionSimpleResponse>>> searchQuestions(
            @RequestParam String keyword) {
        List<QuestionSimpleResponse> questions = questionService.searchQuestions(keyword);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 搜索标签 - 管理员操作
     */
    @GetMapping("/tags/suggest")
    public ResponseEntity<ApiResponse<List<com.lingshu.entity.Tag>>> suggestTags(
            @RequestParam String keyword) {
        List<com.lingshu.entity.Tag> tags = questionService.searchTags(keyword);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    // =================== 兼容旧接口 ===================
    // 使用旧的QuestionRequest对象的接口

    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody com.lingshu.dto.request.QuestionRequest request) {
        QuestionResponse question = questionService.createQuestion(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", question));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody com.lingshu.dto.request.QuestionRequest request) {
        QuestionResponse question = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", question));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<IPage<QuestionResponse>>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty) {

        IPage<QuestionResponse> questions = questionService.getQuestions(
                page, size, keyword, categoryId, difficulty);

        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(@PathVariable Long id) {
        QuestionResponse question = questionService.getQuestion(id);
        return ResponseEntity.ok(ApiResponse.success(question));
    }

    /**
     * 从Excel导入问题
     */
    @PostMapping("/import/excel")
    public ResponseEntity<ApiResponse<List<com.lingshu.entity.Question>>> importQuestionsExcel(
            @RequestParam("file") MultipartFile file) {

        try {
            Long userId = securityUtil.getCurrentUserId();
            List<com.lingshu.entity.Question> questions = 
                    excelImportExportService.importFromExcel(file, userId);

            return ResponseEntity.ok(ApiResponse.success("导入成功", questions));
        } catch (IOException e) {
            log.error("Excel导入失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("导入失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("导入失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("导入失败: " + e.getMessage()));
        }
    }

    /**
     * 从Excel导入问题并归入指定题库
     */
    @PostMapping("/import/excel/bank/{bankId}")
    public ResponseEntity<ApiResponse<List<Question>>> importQuestionsExcelToBank(
            @PathVariable Long bankId,
            @RequestParam("file") MultipartFile file) {

        try {
            Long userId = securityUtil.getCurrentUserId();
            List<Question> questions = excelImportExportService.importFromExcel(file, userId);

            for (Question question : questions) {
                boolean attached = bankService.addQuestionToBank(bankId, question.getId());
                if (!attached) {
                    throw new IllegalStateException("题目已导入，但未能归入当前题库，请检查题库分类配置");
                }
            }

            return ResponseEntity.ok(ApiResponse.success("导入成功", questions));
        } catch (IOException e) {
            log.error("题库Excel导入失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("导入失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("题库导入失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("导入失败: " + e.getMessage()));
        }
    }

    /**
     * 从JSON导入问题
     */
    @PostMapping("/import/json")
    public ResponseEntity<ApiResponse<List<com.lingshu.entity.Question>>> importQuestionsJson(
            @RequestParam("file") MultipartFile file) {

        try {
            Long userId = securityUtil.getCurrentUserId();
            List<com.lingshu.entity.Question> questions = 
                    excelImportExportService.importFromJson(file, userId);

            return ResponseEntity.ok(ApiResponse.success("导入成功", questions));
        } catch (IOException e) {
            log.error("JSON导入失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("导入失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("导入失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("导入失败: " + e.getMessage()));
        }
    }

    /**
     * 导出问题到Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportQuestionsExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) List<Long> questionIds,
            @RequestParam(defaultValue = "false") boolean exportAll) {

        try {
            byte[] excelBytes;

            if (exportAll) {
                // 导出所有问题
                excelBytes = excelImportExportService.exportAllQuestions();
            } else if (questionIds != null && !questionIds.isEmpty()) {
                // 按ID导出
                excelBytes = excelImportExportService.exportToExcel(questionIds);
            } else {
                // 按条件导出
                excelBytes = excelImportExportService.exportQuestionsByCriteria(keyword, categoryId, difficulty);
            }

            // 生成文件名
            String fileName = "questions_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(excelBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (IOException e) {
            log.error("Excel导出失败", e);
            return ResponseEntity.badRequest()
                    .body(("导出失败: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 批量上传题目
     */
    @PostMapping("/batch/upload")
    public ResponseEntity<ApiResponse<List<com.lingshu.entity.Question>>> batchUploadQuestions(
            @RequestParam("file") MultipartFile[] files) {

        try {
            Long userId = securityUtil.getCurrentUserId();
            List<com.lingshu.entity.Question> allQuestions = new ArrayList<>();

            // 处理每个上传的文件
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    List<com.lingshu.entity.Question> questions = 
                            excelImportExportService.importFromExcel(file, userId);
                    allQuestions.addAll(questions);
                }
            }

            return ResponseEntity.ok(ApiResponse.success("上传成功", allQuestions));
        } catch (IOException e) {
            log.error("批量上传失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("上传失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("上传失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("上传失败: " + e.getMessage()));
        }
    }

    /**
     * 下载上传模板
     */
    @GetMapping("/batch/template")
    public ResponseEntity<byte[]> downloadBatchUploadTemplate() {

        try {
            // 生成模板文件
            byte[] templateBytes = excelImportExportService.generateImportTemplate();

            // 生成文件名
            String fileName = "question_upload_template.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(templateBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);

        } catch (IOException e) {
            log.error("模板下载失败", e);
            return ResponseEntity.badRequest()
                    .body(("模板下载失败: " + e.getMessage()).getBytes());
        }
    }
}
