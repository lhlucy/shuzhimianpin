package com.lingshu.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.dto.KeyPoint;
import com.lingshu.dto.request.QuestionCreateRequest;
import com.lingshu.dto.response.QuestionDetailResponse;
import com.lingshu.entity.Question;
import com.lingshu.exception.BusinessException;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportExportService {

    private static final String[] IMPORT_HEADERS = {
            "标题", "描述", "问题", "答案", "难度", "分类ID", "岗位编码", "题型",
            "能力项摘要", "关键词", "标准回答要点", "常见错误点", "可追问方向", "评分关注点",
            "推荐资料", "标签", "关键点", "相关问题ID", "适合模拟面试", "适合岗位刷题",
            "面试出现频率", "来源类型", "可见性", "排序"
    };

    private final QuestionMapper questionMapper;
    private final AdminQuestionService adminQuestionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从Excel导入
     */
    @Transactional
    public List<Question> importFromExcel(MultipartFile file, Long userId) throws IOException {
        List<QuestionCreateRequest> requests = parseExcel(file);
        List<Question> savedQuestions = new ArrayList<>();

        for (QuestionCreateRequest request : requests) {
            try {
                // 创建问题
                QuestionDetailResponse response = adminQuestionService.createQuestion(request, userId);
                // 根据返回的id查询问题
                Question question = questionMapper.selectById(response.getId());
                if (question == null) {
                    throw new RuntimeException("问题创建失败");
                }
                savedQuestions.add(question);
            } catch (Exception e) {
                log.error("导入失败: {}", request.getTitle(), e);
                throw new BusinessException("IMPORT_ERROR", "导入失败: " + e.getMessage());
            }
        }

        return savedQuestions;
    }

    /**
     * 导出到Excel
     */
    public byte[] exportToExcel(List<Long> questionIds) throws IOException {
        List<Question> questions;
        if (questionIds == null || questionIds.isEmpty()) {
            // 导出所有问题
            questions = questionMapper.selectList(null);
        } else {
            // 导出指定问题
            questions = questionMapper.findByIdIn(questionIds);
        }

        return generateExcel(questions);
    }

    /**
     * 解析Excel文件
     */
    private List<QuestionCreateRequest> parseExcel(MultipartFile file) throws IOException {
        List<QuestionCreateRequest> requests = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过表头
            if (rowIterator.hasNext()) {
                rowIterator.next(); // 跳过第一行表头
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue;
                }

                QuestionCreateRequest request = new QuestionCreateRequest();

                // 设置标题
                request.setTitle(getCellValue(row.getCell(0)));

                // 描述
                request.setDescription(getCellValue(row.getCell(1)));

                // 设置问题内容
                request.setQuestionText(getCellValue(row.getCell(2)));

                // 设置答案内容
                request.setAnswerText(getCellValue(row.getCell(3)));

                // 难度默认设为MEDIUM
                String difficulty = getCellValue(row.getCell(4));
                if (difficulty != null && !difficulty.isEmpty()) {
                    request.setDifficulty(difficulty.toUpperCase());
                } else {
                    request.setDifficulty("MEDIUM");
                }

                // 分类ID
                String categoryIdStr = getCellValue(row.getCell(5));
                if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                    try {
                        request.setCategoryId(Long.parseLong(categoryIdStr));
                    } catch (NumberFormatException e) {
                        // 分类ID解析失败
                        // 这里可以使用categoryMapper.findByCategoryName来根据分类名称查找ID
                        // categoryMapper.findByCategoryName(categoryIdStr)
                        //         .ifPresent(category -> request.setCategoryId(category.getId()));
                    }
                }

                request.setPrimaryJobRoleCode(getCellValue(row.getCell(6)));
                request.setQuestionType(getCellValue(row.getCell(7)));
                request.setSkillDimensionSummary(getCellValue(row.getCell(8)));
                request.setKeywords(splitCommaSeparated(getCellValue(row.getCell(9))));
                request.setStandardAnswerPoints(splitMultilineOrPipe(getCellValue(row.getCell(10))));
                request.setCommonMistakes(splitMultilineOrPipe(getCellValue(row.getCell(11))));
                request.setFollowUpPrompts(splitMultilineOrPipe(getCellValue(row.getCell(12))));
                request.setScoringPoints(splitMultilineOrPipe(getCellValue(row.getCell(13))));
                request.setRecommendedResources(splitMultilineOrPipe(getCellValue(row.getCell(14))));

                // 设置标签
                String tagsStr = getCellValue(row.getCell(15));
                if (tagsStr != null && !tagsStr.isEmpty()) {
                    request.setTags(Arrays.asList(tagsStr.split(",")));
                }

                // 解析关键点JSON
                String keyPointsStr = getCellValue(row.getCell(16));
                if (keyPointsStr != null && !keyPointsStr.isEmpty()) {
                    try {
                        List<KeyPoint> keyPoints = objectMapper.readValue(
                                keyPointsStr,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, KeyPoint.class)
                        );
                        request.setKeyPoints(keyPoints);
                    } catch (Exception e) {
                        log.warn("关键点解析失败: {}", keyPointsStr, e);
                    }
                }

                // 解析相关问题ID
                String relatedIdsStr = getCellValue(row.getCell(17));
                if (relatedIdsStr != null && !relatedIdsStr.isEmpty()) {
                    try {
                        List<Long> relatedIds = Arrays.stream(relatedIdsStr.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(Long::parseLong)
                                .collect(Collectors.toList());
                        request.setRelatedQuestionIds(relatedIds);
                    } catch (NumberFormatException e) {
                        log.warn("相关问题ID解析失败: {}", relatedIdsStr, e);
                    }
                }

                String interviewFlag = getCellValue(row.getCell(18));
                if (interviewFlag != null && !interviewFlag.isEmpty()) {
                    request.setIsForInterview(Boolean.parseBoolean(interviewFlag));
                }

                String practiceFlag = getCellValue(row.getCell(19));
                if (practiceFlag != null && !practiceFlag.isEmpty()) {
                    request.setIsForPractice(Boolean.parseBoolean(practiceFlag));
                }

                String frequencyStr = getCellValue(row.getCell(20));
                if (frequencyStr != null && !frequencyStr.isEmpty()) {
                    try {
                        request.setInterviewFrequency(Integer.parseInt(frequencyStr));
                    } catch (NumberFormatException e) {
                        log.warn("面试出现频率解析失败: {}", frequencyStr, e);
                    }
                }

                request.setSourceType(getCellValue(row.getCell(21)));

                // 可见性
                String visibleStr = getCellValue(row.getCell(22));
                if (visibleStr != null && !visibleStr.isEmpty()) {
                    request.setIsVisible(Boolean.parseBoolean(visibleStr));
                }

                // 排序
                String sortOrderStr = getCellValue(row.getCell(23));
                if (sortOrderStr != null && !sortOrderStr.isEmpty()) {
                    try {
                        request.setSortOrder(Integer.parseInt(sortOrderStr));
                    } catch (NumberFormatException e) {
                        log.warn("排序顺序解析失败: {}", sortOrderStr, e);
                    }
                }

                requests.add(request);
            }
        }

        return requests;
    }

    /**
     * 生成Excel文件 - 内部方法
     */
    private byte[] generateExcel(List<Question> questions) throws IOException {
        Workbook workbook = null;
        ByteArrayOutputStream out = null;

        try {
            workbook = new XSSFWorkbook();
            out = new ByteArrayOutputStream();

            Sheet sheet = workbook.createSheet("面试题");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = Arrays.copyOf(IMPORT_HEADERS, IMPORT_HEADERS.length + 2);
            headers[IMPORT_HEADERS.length] = "创建时间";
            headers[IMPORT_HEADERS.length + 1] = "更新时间";

            // 设置表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (Question question : questions) {
                Row row = sheet.createRow(rowNum++);

                // 标题
                row.createCell(0).setCellValue(question.getTitle() != null ? question.getTitle() : "");

                // 描述
                row.createCell(1).setCellValue(question.getDescription() != null ? question.getDescription() : "");

                // 问题
                row.createCell(2).setCellValue(question.getQuestionText() != null ? question.getQuestionText() : "");

                // 答案
                row.createCell(3).setCellValue(question.getAnswerText() != null ? question.getAnswerText() : "");

                // 难度
                row.createCell(4).setCellValue(question.getDifficulty() != null ? question.getDifficulty().name() : "MEDIUM");

                // 分类ID
                row.createCell(5).setCellValue(question.getCategoryId() != null ? question.getCategoryId().toString() : "");

                row.createCell(6).setCellValue("");
                row.createCell(7).setCellValue(question.getQuestionType() != null ? question.getQuestionType() : "");
                row.createCell(8).setCellValue(question.getSkillDimensionSummary() != null ? question.getSkillDimensionSummary() : "");
                row.createCell(9).setCellValue(extractKeywords(question));
                row.createCell(10).setCellValue(formatJsonArray(question.getStandardAnswerPoints()));
                row.createCell(11).setCellValue(formatJsonArray(question.getCommonMistakes()));
                row.createCell(12).setCellValue(formatJsonArray(question.getFollowUpPrompts()));
                row.createCell(13).setCellValue(formatJsonArray(question.getScoringPoints()));
                row.createCell(14).setCellValue(formatJsonArray(question.getRecommendedResources()));

                // 标签
                String tags = "";
                if (question.getTags() != null && !question.getTags().isEmpty()) {
                    tags = question.getTags().stream()
                            .map(tag -> tag != null ? tag.getName() : "")
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.joining(","));
                }
                row.createCell(15).setCellValue(tags);

                // 关键点JSON
                try {
                    String keyPointsJson = "[]";
                    if (question.getKeyPoints() != null) {
                        keyPointsJson = objectMapper.writeValueAsString(question.getKeyPoints());
                    }
                    row.createCell(16).setCellValue(keyPointsJson);
                } catch (Exception e) {
                    row.createCell(16).setCellValue("[]");
                }

                // 相关问题ID
                String relatedIdsStr = "";
                List<Long> relatedIds = question.getRelatedQuestionIdList();
                if (relatedIds != null && !relatedIds.isEmpty()) {
                    relatedIdsStr = relatedIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                }
                row.createCell(17).setCellValue(relatedIdsStr);

                row.createCell(18).setCellValue(Boolean.TRUE.equals(question.getIsForInterview()) ? "true" : "false");
                row.createCell(19).setCellValue(Boolean.TRUE.equals(question.getIsForPractice()) ? "true" : "false");
                row.createCell(20).setCellValue(question.getInterviewFrequency() != null ? question.getInterviewFrequency() : 0);
                row.createCell(21).setCellValue(question.getSourceType() != null ? question.getSourceType() : "");

                // 可见性
                row.createCell(22).setCellValue(Boolean.TRUE.equals(question.getIsVisible()) ? "true" : "false");

                // 排序
                row.createCell(23).setCellValue(question.getSortOrder() != null ? question.getSortOrder() : 0);

                // 创建时间
                row.createCell(24).setCellValue(question.getCreatedAt() != null ?
                        question.getCreatedAt().toString() : "");

                // 更新时间
                row.createCell(25).setCellValue(question.getUpdatedAt() != null ?
                        question.getUpdatedAt().toString() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 将工作簿写入输出流
            workbook.write(out);

        } finally {
            // 关闭资源
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.error("关闭Workbook失败", e);
                }
            }
        }

        // 返回workbook转换的byte数组
        return out != null ? out.toByteArray() : new byte[0];
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字类型
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 判断行是否为空
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 从JSON导入
     */
    @Transactional
    public List<Question> importFromJson(MultipartFile file, Long userId) throws IOException {
        List<QuestionCreateRequest> requests = objectMapper.readValue(
                file.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionCreateRequest.class)
        );

        List<Question> savedQuestions = new ArrayList<>();
        for (QuestionCreateRequest request : requests) {
            try {
                // 创建问题
                adminQuestionService.createQuestion(request, userId);
                Question question = questionMapper.findBySlug(SlugUtil.toSlug(request.getTitle()));
                if (question == null) {
                    throw new RuntimeException("问题创建失败");
                }
                savedQuestions.add(question);
            } catch (Exception e) {
                log.error("导入失败: {}", request.getTitle(), e);
                throw new BusinessException("IMPORT_ERROR", "导入失败: " + e.getMessage());
            }
        }

        return savedQuestions;
    }
    /**
     * 导出所有问题到Excel
     */
    public byte[] exportAllQuestions() throws IOException {
        List<Question> questions = questionMapper.selectList(null);
        log.info("开始导出{}个问题", questions.size());
        return generateExcel(questions);
    }

    /**
     * 按条件导出问题到Excel
     */
    public byte[] exportQuestionsByCriteria(String keyword, Long categoryId, String difficulty) throws IOException {
        List<Question> questions;

        // 解析难度
        Question.Difficulty difficultyEnum = null;
        if (difficulty != null && !difficulty.isEmpty()) {
            try {
                difficultyEnum = Question.Difficulty.valueOf(difficulty.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("难度解析失败: {}", difficulty);
                // 解析失败则设为null
            }
        }

        // 根据条件搜索问题，这里使用searchAll方法
        if (keyword != null || categoryId != null || difficultyEnum != null) {
            // 创建分页对象，获取足够多的记录
            com.baomidou.mybatisplus.core.metadata.IPage<Question> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10000);
            com.baomidou.mybatisplus.core.metadata.IPage<Question> questionPage = questionMapper.searchAll(page, keyword, categoryId, difficultyEnum);
            questions = questionPage.getRecords();
        } else {
            // 无搜索条件时返回空列表
            questions = new ArrayList<>();
            log.warn("无搜索条件，返回空列表");
        }

        log.info("导出问题数量: keyword={}, categoryId={}, difficulty={}, 共{}个问题",
                keyword, categoryId, difficulty, questions.size());
        return generateExcel(questions);
    }

    /**
     * 生成导入模板
     */
    public byte[] generateImportTemplate() throws IOException {
        Workbook workbook = null;
        ByteArrayOutputStream out = null;

        try {
            workbook = new XSSFWorkbook();
            out = new ByteArrayOutputStream();

            Sheet sheet = workbook.createSheet("面试题模板");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = IMPORT_HEADERS;

            // 设置表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("Java多线程原理");
            exampleRow.createCell(1).setCellValue("Java多线程的基本原理和使用方法");
            exampleRow.createCell(2).setCellValue("请详细说明Java多线程的实现原理，以及如何创建和使用线程？");
            exampleRow.createCell(3).setCellValue("Java多线程可以通过继承Thread类或实现Runnable接口来实现...");
            exampleRow.createCell(4).setCellValue("MEDIUM");
            exampleRow.createCell(5).setCellValue("2");
            exampleRow.createCell(6).setCellValue("java_backend");
            exampleRow.createCell(7).setCellValue("TECHNICAL");
            exampleRow.createCell(8).setCellValue("并发编程, 线程模型");
            exampleRow.createCell(9).setCellValue("Java,多线程,线程池");
            exampleRow.createCell(10).setCellValue("线程创建方式|线程池核心参数|线程安全与锁");
            exampleRow.createCell(11).setCellValue("只背概念不讲使用场景|混淆Runnable和Callable");
            exampleRow.createCell(12).setCellValue("线程池参数如何配置？|项目里如何排查死锁？");
            exampleRow.createCell(13).setCellValue("概念准确|能结合项目|能说明风险与优化");
            exampleRow.createCell(14).setCellValue("《Java并发编程实战》|JUC官方文档");
            exampleRow.createCell(15).setCellValue("Java,多线程");
            exampleRow.createCell(16).setCellValue("[]");
            exampleRow.createCell(17).setCellValue("");
            exampleRow.createCell(18).setCellValue("true");
            exampleRow.createCell(19).setCellValue("true");
            exampleRow.createCell(20).setCellValue("8");
            exampleRow.createCell(21).setCellValue("CURATED");
            exampleRow.createCell(22).setCellValue("true");
            exampleRow.createCell(23).setCellValue("1");

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 将工作簿写入输出流
            workbook.write(out);

        } finally {
            // 关闭资源
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.error("关闭Workbook失败", e);
                }
            }
        }

        // 返回模板文件的byte数组
        return out != null ? out.toByteArray() : new byte[0];
    }

    private List<String> splitCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> splitMultilineOrPipe(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split("\\r?\\n|\\|"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String formatJsonArray(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "";
        }
        try {
            List<String> values = objectMapper.readValue(
                    rawValue,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            return String.join("|", values);
        } catch (Exception e) {
            return rawValue;
        }
    }

    private String extractKeywords(Question question) {
        Object keywords = question.getMetadataMap().get("keywords");
        if (keywords instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> keywordList = (List<Object>) keywords;
            return keywordList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        return "";
    }
}
