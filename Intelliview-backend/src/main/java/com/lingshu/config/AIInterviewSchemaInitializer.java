package com.lingshu.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIInterviewSchemaInitializer implements ApplicationRunner {

    private static final String TABLE_NAME = "ai_interviews";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureJobRoleCompetencyModel();
        ensureDefaultJobRoles();
        ensureSeedInterviewQuestions();
        ensureEvaluationCalibrationTables();
        if (!tableExists(TABLE_NAME)) {
            log.warn("表 {} 不存在，跳过模拟面试增量字段检查", TABLE_NAME);
            return;
        }

        ensureColumn(
                "interview_language",
                "ALTER TABLE ai_interviews ADD COLUMN interview_language varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '中文' AFTER target_position"
        );
        ensureColumn(
                "brushed_question_ids",
                "ALTER TABLE ai_interviews ADD COLUMN brushed_question_ids json NULL AFTER skill_tags"
        );
        ensureColumn(
                "brushed_question_summary",
                "ALTER TABLE ai_interviews ADD COLUMN brushed_question_summary json NULL AFTER brushed_question_ids"
        );
        ensureColumn(
                "resume_file_name",
                "ALTER TABLE ai_interviews ADD COLUMN resume_file_name varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER voice_enabled"
        );
        ensureColumn(
                "resume_content",
                "ALTER TABLE ai_interviews ADD COLUMN resume_content text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER resume_file_name"
        );
        ensureColumn(
                "interviewer_profile",
                "ALTER TABLE ai_interviews ADD COLUMN interviewer_profile text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER resume_content"
        );
        ensureColumn(
                "opening_message",
                "ALTER TABLE ai_interviews ADD COLUMN opening_message text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER interviewer_profile"
        );
    }

    private void ensureJobRoleCompetencyModel() {
        if (!tableExists("job_roles") || columnExists("job_roles", "competency_model")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE job_roles ADD COLUMN competency_model json NULL AFTER interview_focus");
        log.info("已自动补齐 job_roles.competency_model 字段");
    }

    private void ensureDefaultJobRoles() {
        if (!tableExists("job_roles")) {
            return;
        }
        seedRole("java_backend", "Java后端开发", "面向企业级服务端、微服务和高并发业务系统的后端岗位", "校招 / 0-3年", "backend", "ENTRY",
                jsonArray("Java", "Spring Boot", "Spring Cloud", "MySQL", "Redis", "Kafka", "JVM"),
                jsonArray("Java基础", "并发与JVM", "Spring生态", "数据库与缓存", "微服务", "项目表达"));
        seedRole("frontend", "前端开发", "面向 Web 应用、管理后台和跨端交互体验的前端岗位", "校招 / 0-3年", "frontend", "ENTRY",
                jsonArray("JavaScript", "TypeScript", "Vue 3", "React", "Vite", "Node.js", "性能优化"),
                jsonArray("语言与浏览器基础", "框架原理", "工程化", "组件设计", "性能优化", "项目表达"));
        seedRole("python", "Python开发", "面向服务端开发、自动化和数据处理的 Python 岗位", "校招 / 0-3年", "backend", "ENTRY",
                jsonArray("Python", "FastAPI", "Django", "SQLAlchemy", "MySQL", "Redis", "Celery"),
                jsonArray("语言特性", "Web框架", "异步任务", "数据处理", "工程实践", "项目表达"));
        seedRole("algorithm", "算法工程师", "面向推荐、搜索、CV/NLP 和机器学习建模落地的算法岗位", "校招 / 1-3年", "algorithm", "INTERMEDIATE",
                jsonArray("Python", "C++", "机器学习", "深度学习", "PyTorch", "特征工程", "模型评估"),
                jsonArray("算法基础", "机器学习", "模型训练", "评估指标", "数据处理", "工程落地"));
        seedRole("ai_large_model", "大模型应用工程师", "面向 Agent、RAG、提示词工程和企业大模型应用落地的热门 AI 岗位", "校招 / 0-3年", "ai", "INTERMEDIATE",
                jsonArray("Python", "LangChain", "RAG", "Vector DB", "Prompt Engineering", "FastAPI", "OpenAI API", "vLLM"),
                jsonArray("LLM基础", "RAG检索增强", "Agent工具调用", "提示词设计", "评测与安全", "业务落地"));
        seedRole("mlops_engineer", "MLOps工程师", "面向模型训练、部署、监控和持续交付的 AI 工程化岗位", "校招 / 1-3年", "ai", "INTERMEDIATE",
                jsonArray("Python", "Docker", "Kubernetes", "MLflow", "Airflow", "Kubeflow", "Prometheus"),
                jsonArray("训练流水线", "模型部署", "监控回滚", "容器化", "数据版本", "成本优化"));
        seedRole("data_engineer", "数据开发工程师", "面向数仓、湖仓、实时计算和数据平台建设的数据岗位", "校招 / 0-3年", "data", "ENTRY",
                jsonArray("SQL", "Python", "Spark", "Flink", "Hive", "Kafka", "ClickHouse", "Doris"),
                jsonArray("SQL能力", "批流一体", "数据建模", "调度治理", "实时计算", "数据质量"));
        seedRole("data_analyst", "数据分析师", "面向业务指标、用户增长和经营分析的数据岗位", "校招 / 0-3年", "data", "ENTRY",
                jsonArray("SQL", "Python", "Excel", "Tableau", "Power BI", "A/B Test", "统计学"),
                jsonArray("指标体系", "SQL取数", "统计分析", "可视化", "业务洞察", "实验分析"));
        seedRole("bi_engineer", "BI工程师", "面向指标平台、报表体系和自助分析建设的数据产品岗位", "校招 / 0-3年", "data", "ENTRY",
                jsonArray("SQL", "ETL", "Tableau", "Power BI", "FineBI", "Doris", "数据建模"),
                jsonArray("指标口径", "数据建模", "报表性能", "权限治理", "可视化表达", "业务协同"));
        seedRole("cloud_native", "云原生开发工程师", "面向容器、微服务、服务网格和云平台开发的岗位", "校招 / 1-3年", "cloud", "INTERMEDIATE",
                jsonArray("Go", "Kubernetes", "Docker", "Istio", "gRPC", "Prometheus", "Helm"),
                jsonArray("容器原理", "K8s资源", "服务治理", "可观测性", "云平台", "故障定位"));
        seedRole("devops_sre", "DevOps/SRE工程师", "面向 CI/CD、稳定性治理、监控告警和自动化运维的岗位", "校招 / 0-3年", "ops", "INTERMEDIATE",
                jsonArray("Linux", "Docker", "Kubernetes", "Jenkins", "GitLab CI", "Terraform", "Prometheus", "Grafana"),
                jsonArray("CI/CD", "Linux排障", "监控告警", "容量规划", "自动化", "故障复盘"));
        seedRole("cybersecurity", "网络安全工程师", "面向安全运营、渗透测试、漏洞治理和云安全的岗位", "校招 / 0-3年", "security", "INTERMEDIATE",
                jsonArray("Linux", "Web安全", "OWASP Top 10", "Burp Suite", "Nmap", "SIEM", "云安全"),
                jsonArray("漏洞原理", "渗透测试", "安全运营", "应急响应", "权限与加密", "合规意识"));
        seedRole("fullstack", "全栈开发工程师", "面向中小团队端到端交付和快速迭代的全栈岗位", "校招 / 0-3年", "fullstack", "ENTRY",
                jsonArray("TypeScript", "Vue", "React", "Node.js", "NestJS", "Spring Boot", "MySQL", "Docker"),
                jsonArray("前后端协作", "接口设计", "组件开发", "数据库设计", "部署交付", "工程质量"));
        seedRole("go_backend", "Go后端开发", "面向高并发网关、云原生基础设施和微服务的 Go 后端岗位", "校招 / 0-3年", "backend", "ENTRY",
                jsonArray("Go", "Gin", "gRPC", "MySQL", "Redis", "Kafka", "Kubernetes"),
                jsonArray("Go并发", "接口设计", "微服务", "缓存与消息", "性能调优", "工程实践"));
        seedRole("c_cpp", "C/C++开发工程师", "面向基础软件、音视频、游戏引擎和高性能计算的岗位", "校招 / 0-3年", "backend", "INTERMEDIATE",
                jsonArray("C++", "STL", "Linux", "网络编程", "多线程", "CMake", "性能调优"),
                jsonArray("内存管理", "并发编程", "网络IO", "数据结构", "性能分析", "工程构建"));
        seedRole("mobile", "移动端开发工程师", "面向 Android/iOS 和跨端应用开发的岗位", "校招 / 0-3年", "mobile", "ENTRY",
                jsonArray("Kotlin", "Swift", "Flutter", "React Native", "Android", "iOS", "性能优化"),
                jsonArray("页面架构", "生命周期", "网络与缓存", "跨端方案", "性能优化", "发布质量"));
        seedRole("qa_test", "测试开发工程师", "面向自动化测试、质量平台和持续质量保障的岗位", "校招 / 0-3年", "qa", "ENTRY",
                jsonArray("Python", "Java", "Selenium", "Playwright", "JMeter", "Pytest", "CI/CD"),
                jsonArray("测试设计", "自动化框架", "接口测试", "性能测试", "质量度量", "缺陷定位"));
        seedRole("database_dba", "数据库工程师/DBA", "面向数据库运维、性能优化、备份恢复和数据可靠性的岗位", "校招 / 1-3年", "data", "INTERMEDIATE",
                jsonArray("MySQL", "PostgreSQL", "Redis", "MongoDB", "SQL优化", "备份恢复", "高可用"),
                jsonArray("索引优化", "事务隔离", "慢查询治理", "备份恢复", "高可用架构", "容量规划"));
        seedRole("embedded_iot", "嵌入式/物联网工程师", "面向智能硬件、车载和物联网终端开发的岗位", "校招 / 0-3年", "embedded", "ENTRY",
                jsonArray("C", "C++", "RTOS", "Linux驱动", "ARM", "MQTT", "BLE"),
                jsonArray("C语言基础", "驱动开发", "实时系统", "通信协议", "功耗优化", "硬件调试"));
        seedRole("product_tech", "技术产品经理", "面向 AI 产品、开发者工具、数据平台和技术型产品规划的岗位", "校招 / 0-3年", "product", "ENTRY",
                jsonArray("SQL", "Axure", "Figma", "数据分析", "API设计", "A/B Test", "AI产品"),
                jsonArray("需求分析", "技术理解", "原型设计", "指标评估", "跨团队沟通", "落地复盘"));

        jdbcTemplate.update("""
                UPDATE job_roles
                SET is_active = TRUE, updated_at = NOW()
                WHERE code IN (
                    'java_backend','frontend','python','algorithm','ai_large_model','mlops_engineer','data_engineer',
                    'data_analyst','bi_engineer','cloud_native','devops_sre','cybersecurity','fullstack','go_backend',
                    'c_cpp','mobile','qa_test','database_dba','embedded_iot','product_tech'
                )
                """);
        ensureDefaultCompetencyModels();
        ensureDefaultSkillDimensions();
    }

    private void ensureDefaultCompetencyModels() {
        if (!columnExists("job_roles", "competency_model")) {
            return;
        }
        updateCompetencyModel("java_backend", 30, 25, 25, 10, 10);
        updateCompetencyModel("frontend", 28, 22, 20, 15, 15);
        updateCompetencyModel("python", 27, 25, 22, 13, 13);
        updateCompetencyModel("algorithm", 35, 20, 25, 8, 12);
        updateCompetencyModel("ai_large_model", 30, 25, 25, 10, 10);
        updateCompetencyModel("mlops_engineer", 28, 22, 30, 8, 12);
        updateCompetencyModel("data_engineer", 28, 24, 28, 8, 12);
        updateCompetencyModel("data_analyst", 20, 25, 25, 20, 10);
        updateCompetencyModel("bi_engineer", 22, 26, 24, 18, 10);
        updateCompetencyModel("cloud_native", 32, 20, 30, 8, 10);
        updateCompetencyModel("devops_sre", 28, 18, 34, 10, 10);
        updateCompetencyModel("cybersecurity", 30, 20, 30, 10, 10);
        updateCompetencyModel("fullstack", 26, 26, 24, 12, 12);
        updateCompetencyModel("go_backend", 32, 22, 26, 8, 12);
        updateCompetencyModel("c_cpp", 35, 18, 28, 7, 12);
        updateCompetencyModel("mobile", 26, 26, 23, 13, 12);
        updateCompetencyModel("qa_test", 24, 22, 30, 14, 10);
        updateCompetencyModel("database_dba", 32, 20, 32, 6, 10);
        updateCompetencyModel("embedded_iot", 34, 22, 26, 8, 10);
        updateCompetencyModel("product_tech", 16, 28, 24, 22, 10);
    }

    private void updateCompetencyModel(String roleCode, int technicalDepth, int projectRelevance,
                                       int problemSolving, int communicationClarity, int jobMatch) {
        jdbcTemplate.update("""
                UPDATE job_roles
                SET competency_model = JSON_OBJECT(
                    'weights', JSON_OBJECT(
                        'technicalDepth', ?,
                        'projectRelevance', ?,
                        'problemSolving', ?,
                        'communicationClarity', ?,
                        'jobMatch', ?
                    )
                ),
                updated_at = NOW()
                WHERE code = ?
                """, technicalDepth, projectRelevance, problemSolving, communicationClarity, jobMatch, roleCode);
    }

    private void ensureDefaultSkillDimensions() {
        if (!tableExists("job_role_skill_dimensions")) {
            return;
        }
        removeLegacySkillDimensions("java_backend", "language", "jvm_concurrency", "framework", "database", "system_design", "project");
        removeLegacySkillDimensions("frontend", "language_browser", "framework", "engineering", "performance", "project");
        removeLegacySkillDimensions("python", "language", "framework", "data_processing", "engineering", "project");
        removeLegacySkillDimensions("algorithm", "algo_basic", "ml", "dl", "data_processing", "evaluation", "llm_rag", "project");

        seedSkillDimension("java_backend", "basic_knowledge", "基础知识", 30, "Java语法、集合、并发、JVM与Spring核心机制");
        seedSkillDimension("java_backend", "project_experience", "项目经验", 25, "服务端项目职责、接口设计、数据库与缓存落地");
        seedSkillDimension("java_backend", "system_design", "系统设计", 25, "高并发、高可用、微服务、事务与消息一致性");
        seedSkillDimension("java_backend", "coding_ability", "编码能力", 20, "代码质量、边界处理、调试与工程实践");

        seedSkillDimension("frontend", "browser_language", "语言与浏览器", 25, "JavaScript/TypeScript、DOM、网络与渲染机制");
        seedSkillDimension("frontend", "framework_component", "框架与组件", 25, "Vue/React原理、组件设计、状态管理与路由");
        seedSkillDimension("frontend", "engineering_quality", "工程化质量", 20, "构建工具、规范、测试、CI/CD与可维护性");
        seedSkillDimension("frontend", "performance_experience", "性能与体验", 15, "加载优化、渲染优化、交互体验与监控定位");
        seedSkillDimension("frontend", "project_delivery", "项目交付", 15, "需求理解、跨端适配、协作沟通与项目复盘");

        seedSkillDimension("python", "language_async", "语言与异步", 25, "Python语法、类型、异步编程与标准库");
        seedSkillDimension("python", "web_framework", "Web框架", 25, "FastAPI/Django架构、接口设计、鉴权与异常处理");
        seedSkillDimension("python", "data_task", "数据与任务", 20, "数据处理、脚本自动化、任务调度与消息队列");
        seedSkillDimension("python", "engineering_practice", "工程实践", 15, "测试、部署、日志、性能与可维护性");
        seedSkillDimension("python", "project_fit", "项目落地", 15, "业务理解、技术取舍和项目成果表达");

        seedSkillDimension("algorithm", "algo_basic", "算法基础", 20, "数据结构、复杂度、常见算法题型与编码实现");
        seedSkillDimension("algorithm", "model_method", "模型方法", 25, "机器学习、深度学习、特征工程与调参思路");
        seedSkillDimension("algorithm", "experiment_evaluation", "实验评估", 20, "指标设计、离线评估、A/B测试与误差分析");
        seedSkillDimension("algorithm", "engineering_deploy", "工程落地", 20, "模型部署、推理优化、数据管线与可维护性");
        seedSkillDimension("algorithm", "business_impact", "业务价值", 15, "场景理解、指标提升、效果复盘与收益表达");

        seedSkillDimension("ai_large_model", "llm_basic", "大模型基础", 22, "Transformer、上下文、推理参数、微调与评测基础");
        seedSkillDimension("ai_large_model", "rag_agent", "RAG与Agent", 28, "文档切分、向量检索、rerank、工具调用与流程编排");
        seedSkillDimension("ai_large_model", "prompt_eval", "提示词与评测", 18, "Prompt设计、结构化输出、自动评测与人工校验");
        seedSkillDimension("ai_large_model", "engineering_delivery", "工程交付", 17, "接口封装、权限、日志、限流、成本与延迟优化");
        seedSkillDimension("ai_large_model", "scenario_value", "场景价值", 15, "业务场景理解、可追溯性、安全与落地效果");

        seedSkillDimension("mlops_engineer", "pipeline_training", "训练流水线", 25, "数据版本、实验追踪、训练编排与自动化");
        seedSkillDimension("mlops_engineer", "deployment_serving", "模型部署", 25, "Docker/K8s、模型服务、灰度发布与弹性伸缩");
        seedSkillDimension("mlops_engineer", "monitoring_governance", "监控治理", 20, "性能监控、漂移检测、告警、回滚与审计");
        seedSkillDimension("mlops_engineer", "platform_engineering", "平台工程", 18, "MLflow/Kubeflow/Airflow集成、权限与多环境管理");
        seedSkillDimension("mlops_engineer", "cost_reliability", "成本与可靠性", 12, "资源利用、SLA、容量规划与故障复盘");

        seedSkillDimension("data_engineer", "sql_modeling", "SQL与建模", 25, "SQL能力、维度建模、指标口径和数仓分层");
        seedSkillDimension("data_engineer", "batch_stream", "批流计算", 25, "Spark/Flink/Hive/Kafka链路与状态一致性");
        seedSkillDimension("data_engineer", "data_quality", "数据质量", 20, "校验规则、血缘、监控告警和异常修复");
        seedSkillDimension("data_engineer", "platform_governance", "平台治理", 15, "调度、权限、元数据、成本和稳定性治理");
        seedSkillDimension("data_engineer", "business_delivery", "业务交付", 15, "需求拆解、指标落地和跨团队协作");

        seedSkillDimension("data_analyst", "metrics_business", "指标与业务", 28, "指标体系、业务问题定义和结论可行动性");
        seedSkillDimension("data_analyst", "sql_analysis", "SQL与分析", 22, "取数、清洗、统计分析和异常归因");
        seedSkillDimension("data_analyst", "experiment_method", "实验方法", 18, "A/B测试、因果判断、样本和显著性分析");
        seedSkillDimension("data_analyst", "visual_story", "可视化表达", 17, "报表设计、图表表达和汇报叙事");
        seedSkillDimension("data_analyst", "tool_delivery", "工具交付", 15, "Excel/Python/BI工具使用和分析流程沉淀");

        seedSkillDimension("bi_engineer", "indicator_definition", "指标口径", 25, "指标定义、口径治理、维度层级与权限范围");
        seedSkillDimension("bi_engineer", "data_model", "数据建模", 25, "宽表、星型模型、聚合策略和数据集设计");
        seedSkillDimension("bi_engineer", "dashboard_delivery", "报表交付", 20, "可视化布局、筛选联动、性能和易用性");
        seedSkillDimension("bi_engineer", "performance_governance", "性能治理", 15, "查询优化、缓存、权限和发布流程");
        seedSkillDimension("bi_engineer", "business_collaboration", "业务协同", 15, "需求澄清、验收、培训和持续迭代");

        seedSkillDimension("cloud_native", "container_k8s", "容器与K8s", 28, "容器原理、Pod/Service/Ingress、调度与资源管理");
        seedSkillDimension("cloud_native", "microservice_governance", "微服务治理", 22, "服务发现、配置、限流、熔断、网关与服务网格");
        seedSkillDimension("cloud_native", "observability", "可观测性", 20, "日志、指标、Trace、Prometheus与故障定位");
        seedSkillDimension("cloud_native", "platform_delivery", "平台交付", 18, "Helm、CI/CD、灰度、弹性伸缩与多环境管理");
        seedSkillDimension("cloud_native", "reliability", "可靠性设计", 12, "高可用、容量、灾备和风险预案");

        seedSkillDimension("devops_sre", "cicd_release", "CI/CD发布", 22, "流水线、制品、环境、灰度发布与回滚");
        seedSkillDimension("devops_sre", "linux_troubleshooting", "系统排障", 22, "Linux、网络、进程、磁盘和容器问题定位");
        seedSkillDimension("devops_sre", "monitoring_slo", "监控与SLO", 24, "SLI/SLO、告警分级、仪表盘和错误预算");
        seedSkillDimension("devops_sre", "automation_iac", "自动化与IaC", 17, "脚本、Terraform、配置管理和标准化运维");
        seedSkillDimension("devops_sre", "incident_review", "故障复盘", 15, "应急响应、止损、根因分析和复盘改进");

        seedSkillDimension("cybersecurity", "vulnerability_principle", "漏洞原理", 25, "Web安全、OWASP、权限绕过、注入和XSS原理");
        seedSkillDimension("cybersecurity", "penetration_testing", "渗透测试", 22, "信息收集、利用验证、工具使用和报告输出");
        seedSkillDimension("cybersecurity", "security_operations", "安全运营", 20, "日志分析、SIEM、告警研判和威胁狩猎");
        seedSkillDimension("cybersecurity", "incident_response", "应急响应", 18, "证据保全、止损、溯源、修复和复盘");
        seedSkillDimension("cybersecurity", "compliance_awareness", "合规意识", 15, "数据安全、权限控制、加密和安全规范");

        seedSkillDimension("fullstack", "frontend_delivery", "前端交付", 22, "页面组件、状态管理、交互体验和性能");
        seedSkillDimension("fullstack", "backend_api", "后端接口", 24, "API设计、鉴权、业务逻辑、数据库与缓存");
        seedSkillDimension("fullstack", "system_integration", "系统集成", 20, "前后端联调、错误处理、部署和可观测性");
        seedSkillDimension("fullstack", "engineering_quality", "工程质量", 18, "代码结构、测试、构建、规范和可维护性");
        seedSkillDimension("fullstack", "product_thinking", "产品理解", 16, "需求拆解、优先级、用户体验和迭代复盘");

        seedSkillDimension("go_backend", "go_language", "Go语言基础", 28, "goroutine、channel、context、内存和错误处理");
        seedSkillDimension("go_backend", "api_microservice", "接口与微服务", 22, "Gin/gRPC、服务治理、协议设计和中间件");
        seedSkillDimension("go_backend", "storage_mq", "存储与消息", 18, "MySQL、Redis、Kafka、事务和一致性");
        seedSkillDimension("go_backend", "performance_concurrency", "并发与性能", 20, "pprof、锁竞争、泄漏、压测和调优");
        seedSkillDimension("go_backend", "engineering_practice", "工程实践", 12, "测试、部署、日志、监控和项目表达");

        seedSkillDimension("c_cpp", "language_memory", "语言与内存", 30, "C/C++语法、STL、指针、RAII和内存管理");
        seedSkillDimension("c_cpp", "concurrency_network", "并发与网络", 22, "多线程、锁、网络IO、事件驱动和协议处理");
        seedSkillDimension("c_cpp", "performance_debug", "性能与调试", 22, "性能分析、崩溃定位、工具链和调优方法");
        seedSkillDimension("c_cpp", "system_design", "系统设计", 16, "模块划分、资源管理、稳定性和可扩展性");
        seedSkillDimension("c_cpp", "project_build", "工程构建", 10, "CMake、依赖管理、测试和项目复盘");

        seedSkillDimension("mobile", "platform_basic", "平台基础", 24, "Android/iOS生命周期、线程模型、系统组件和权限");
        seedSkillDimension("mobile", "ui_architecture", "页面架构", 24, "UI组件、架构模式、状态管理和跨端方案");
        seedSkillDimension("mobile", "network_storage", "网络与缓存", 18, "网络请求、本地存储、离线能力和数据同步");
        seedSkillDimension("mobile", "performance_quality", "性能与质量", 19, "启动、卡顿、内存、包体和稳定性优化");
        seedSkillDimension("mobile", "release_project", "发布与项目", 15, "渠道发布、灰度、埋点、问题排查和复盘");

        seedSkillDimension("qa_test", "test_design", "测试设计", 25, "用例设计、风险分析、边界场景和覆盖策略");
        seedSkillDimension("qa_test", "automation_framework", "自动化框架", 25, "接口/UI自动化、数据驱动、报告和维护性");
        seedSkillDimension("qa_test", "performance_quality", "性能与质量", 18, "压测、瓶颈分析、质量指标和持续改进");
        seedSkillDimension("qa_test", "ci_integration", "CI集成", 17, "流水线、环境、Mock、契约测试和稳定性治理");
        seedSkillDimension("qa_test", "defect_analysis", "缺陷定位", 15, "日志、链路、数据库验证和协作推动修复");

        seedSkillDimension("database_dba", "sql_index", "SQL与索引", 28, "执行计划、索引设计、慢查询和SQL改写");
        seedSkillDimension("database_dba", "transaction_lock", "事务与锁", 20, "隔离级别、锁等待、死锁和一致性");
        seedSkillDimension("database_dba", "ha_backup", "高可用与备份", 22, "主从、复制、备份恢复、容灾和演练");
        seedSkillDimension("database_dba", "monitor_capacity", "监控与容量", 18, "监控告警、容量规划、参数调优和稳定性");
        seedSkillDimension("database_dba", "security_governance", "安全治理", 12, "权限、审计、脱敏和数据合规");

        seedSkillDimension("embedded_iot", "c_hardware", "C语言与硬件", 26, "C/C++、寄存器、外设、调试和硬件接口");
        seedSkillDimension("embedded_iot", "rtos_driver", "RTOS与驱动", 24, "任务调度、中断、驱动模型和实时性");
        seedSkillDimension("embedded_iot", "communication_protocol", "通信协议", 20, "MQTT/BLE/CAN/UART/SPI/I2C和网络链路");
        seedSkillDimension("embedded_iot", "power_reliability", "功耗与可靠性", 16, "低功耗、异常恢复、稳定性和量产问题");
        seedSkillDimension("embedded_iot", "project_debug", "项目调试", 14, "示波器/逻辑分析仪、日志、联调和问题复盘");

        seedSkillDimension("product_tech", "requirement_analysis", "需求分析", 25, "用户场景、痛点拆解、优先级和范围管理");
        seedSkillDimension("product_tech", "technical_understanding", "技术理解", 22, "API、数据、AI能力边界、成本和实现复杂度");
        seedSkillDimension("product_tech", "prototype_design", "原型设计", 18, "信息架构、流程、交互原型和验收标准");
        seedSkillDimension("product_tech", "metrics_experiment", "指标与实验", 18, "北极星指标、A/B测试、数据分析和效果复盘");
        seedSkillDimension("product_tech", "cross_team_delivery", "跨团队交付", 17, "沟通协同、风险管理、节奏推进和上线复盘");
    }

    private void seedSkillDimension(String roleCode, String code, String name, int weight, String description) {
        jdbcTemplate.update("""
                INSERT INTO job_role_skill_dimensions
                    (job_role_id, dimension_code, dimension_name, weight, description, created_at, updated_at)
                SELECT jr.id, ?, ?, ?, ?, NOW(), NOW()
                FROM job_roles jr
                WHERE jr.code = ?
                LIMIT 1
                ON DUPLICATE KEY UPDATE
                    dimension_name = VALUES(dimension_name),
                    weight = VALUES(weight),
                    description = VALUES(description),
                    updated_at = NOW()
                """, code, name, weight, description, roleCode);
    }

    private void removeLegacySkillDimensions(String roleCode, String... dimensionCodes) {
        for (String dimensionCode : dimensionCodes) {
            jdbcTemplate.update("""
                    DELETE d
                    FROM job_role_skill_dimensions d
                    JOIN job_roles jr ON jr.id = d.job_role_id
                    WHERE jr.code = ? AND d.dimension_code = ?
                    """, roleCode, dimensionCode);
        }
    }

    private void seedRole(String code, String name, String description, String experience, String category,
                          String difficulty, String techStack, String focus) {
        jdbcTemplate.update("""
                INSERT INTO job_roles
                    (code, name, description, applicable_experience, category, difficulty_level, typical_tech_stack, interview_focus, is_active, created_at, updated_at)
                SELECT ?, ?, ?, ?, ?, ?, ?, ?, TRUE, NOW(), NOW()
                WHERE NOT EXISTS (SELECT 1 FROM job_roles WHERE code = ? LIMIT 1)
                """, code, name, description, experience, category, difficulty, techStack, focus, code);
    }

    private void ensureSeedInterviewQuestions() {
        if (!tableExists("questions") || !tableExists("job_roles")) {
            return;
        }
        if (!columnExists("questions", "primary_job_role_id") || !columnExists("questions", "standard_answer_points")) {
            log.warn("questions 表岗位化字段不完整，跳过岗位题目种子数据补齐");
            return;
        }
        seedQuestion("ai_large_model", "RAG 问答系统如何降低幻觉并提升可追溯性？",
                "面向大模型应用工程师的 RAG 架构题",
                "请说明一个企业知识库 RAG 系统从文档入库到答案生成的链路，并重点解释如何降低幻觉、提升答案可追溯性。",
                "参考答案应覆盖：1. 文档解析、清洗、切分和元数据保留，切分粒度要兼顾语义完整性与召回效率；2. 使用 embedding 建索引，查询侧做 query rewrite、多路召回、rerank，并保留来源片段；3. 提示词中要求模型只基于检索证据回答，不足时明确说明无法判断；4. 输出引用来源、文档标题、段落或链接，方便追溯；5. 通过离线评测集统计召回率、答案忠实度、引用准确率，并做人工抽检；6. 对敏感内容做权限过滤，避免越权检索。",
                "MEDIUM", "TECHNICAL", 96);
        seedQuestion("java_backend", "Spring Boot 接口突然变慢你如何定位？",
                "面向 Java 后端的性能排查题",
                "线上某个 Spring Boot 接口 P95 从 120ms 升到 2s，你会按什么顺序定位和修复？",
                "真实排查路径通常是：先确认监控指标和影响范围，区分是单接口、单实例还是全链路；查看应用日志、APM trace、GC 日志和线程池状态；继续定位数据库慢 SQL、索引失效、锁等待、外部 RPC、Redis/MQ 抖动等依赖；若是代码问题，关注 N+1 查询、同步阻塞、锁粒度、序列化和大对象；修复后通过压测或灰度验证 P95/P99、错误率和资源占用，最后补充告警与回归用例。",
                "MEDIUM", "SCENARIO", 94);
        seedQuestion("frontend", "Vue3 响应式原理和性能优化怎么讲？",
                "面向前端开发的框架原理题",
                "请解释 Vue3 响应式系统的核心机制，并说明大列表或复杂表单场景下的性能优化思路。",
                "Vue3 使用 Proxy 拦截对象读写，在读取时通过 track 收集当前 effect 与属性依赖，在写入时通过 trigger 触发相关 effect 更新；ref 通过 value 访问器维护依赖。性能优化可从减少响应式开销和减少渲染开销两侧入手：大静态数据用 shallowRef/shallowReactive 或 markRaw，列表使用虚拟滚动、稳定 key、分页加载，复杂计算使用 computed 缓存，组件拆分时控制 props 变化范围，事件与请求做防抖节流，并用 devtools/performance profile 定位真正瓶颈。",
                "MEDIUM", "TECHNICAL", 92);
        seedQuestion("data_engineer", "Flink 实时数仓如何保证端到端一致性？",
                "面向数据开发工程师的实时计算题",
                "请说明 Kafka + Flink + OLAP 实时链路中如何尽量保证端到端一致性和可恢复性。",
                "关键点包括：Kafka source 使用 checkpoint 记录 offset，Flink 开启 checkpoint 和状态后端，算子状态可恢复；写出端优先选择支持两阶段提交或幂等写入的 sink；业务主键、去重键和窗口水位线要设计清楚，处理乱序和迟到数据；维表关联要考虑缓存过期和版本一致性；失败恢复后验证是否重复消费、重复写入或丢数；同时建设数据质量校验，例如源端条数、落地条数、延迟、空值率和异常波动告警。",
                "HARD", "TECHNICAL", 88);
        seedQuestion("cybersecurity", "如何排查一次疑似 SQL 注入攻击？",
                "面向网络安全工程师的应急响应题",
                "业务告警显示某接口出现大量异常 SQL 片段和 500 错误，你会如何确认、处置和复盘？",
                "合理答案应包含：先保存日志、请求样本、时间线和受影响资产，避免证据丢失；检查 Web 访问日志、WAF 告警、应用日志和数据库审计，确认 payload、来源 IP、参数位置和是否成功利用；临时处置包括 WAF 规则、限流、封禁异常来源、下线高危接口或回滚版本；代码层修复应使用参数化查询/ORM 绑定变量，避免字符串拼接，并补充输入校验；排查数据泄露和权限扩大风险；最后做根因复盘、安全测试用例、监控规则和开发规范更新。",
                "MEDIUM", "SCENARIO", 86);
        seedQuestion("cloud_native", "Kubernetes Pod 一直 CrashLoopBackOff 怎么定位？",
                "面向云原生开发的 K8s 排障题",
                "某服务发布后 Pod 进入 CrashLoopBackOff，你会如何快速定位原因？",
                "排查顺序：kubectl get/describe pod 查看事件、镜像拉取、探针失败、OOMKilled、调度和挂载问题；kubectl logs --previous 查看上一次容器退出前日志；确认启动命令、环境变量、配置文件、Secret/ConfigMap、端口和依赖服务是否正确；如果是资源不足，查看 requests/limits、节点资源和容器退出码；如果是探针配置过严，调整 initialDelaySeconds、timeoutSeconds 或检查健康接口；修复后观察 rollout 状态、重启次数和服务流量。",
                "MEDIUM", "SCENARIO", 84);
        seedQuestion("devops_sre", "如何设计服务的监控告警体系？",
                "面向 DevOps/SRE 的稳定性题",
                "请为一个核心支付/下单服务设计监控告警体系，说明你会关注哪些指标。",
                "应围绕用户体验和系统健康设计：SLI/SLO 包括可用性、错误率、延迟 P95/P99、吞吐量；基础资源包括 CPU、内存、磁盘、网络、连接数；依赖包括数据库慢查询、缓存命中率、MQ 堆积、外部接口超时；告警要分级，避免单点指标噪音，优先使用多条件组合和持续时间；配套仪表盘、日志 trace 关联、值班升级、故障演练和复盘机制；变更发布时监控错误率和延迟是否异常。",
                "MEDIUM", "SYSTEM_DESIGN", 82);
        seedQuestion("qa_test", "接口自动化测试框架怎么设计？",
                "面向测试开发工程师的工程设计题",
                "请设计一个可在 CI 中运行的接口自动化测试框架，说明分层和关键能力。",
                "一个可落地框架通常包含：用例层描述业务场景和断言，客户端层封装鉴权、请求、重试和日志，数据层管理测试数据准备与清理，配置层区分环境和密钥；断言不仅检查状态码，还检查业务字段、数据库或消息副作用；支持参数化、前后置钩子、报告生成和失败截图/日志；CI 中按冒烟、回归、全量分层运行，失败时产出可定位的请求响应和链路信息；对不稳定外部依赖可使用 mock 或契约测试。",
                "MEDIUM", "SYSTEM_DESIGN", 80);
        seedQuestion("go_backend", "Go goroutine 泄漏如何发现和修复？",
                "面向 Go 后端开发的并发题",
                "请说明 goroutine 泄漏的常见原因，以及你会如何定位和修复。",
                "常见原因有 channel 无接收方或无发送方导致阻塞、context 未取消、定时器/ticker 未 Stop、后台循环缺少退出条件、网络请求无超时。定位方式包括查看 goroutine 数量趋势、pprof goroutine dump、阻塞栈、trace 和压测复现。修复时为请求链路传递 context，select 中监听 ctx.Done，为 IO 设置 timeout，明确 channel 关闭责任，ticker defer Stop，后台 worker 用 wait group 和关闭信号管理生命周期。",
                "MEDIUM", "TECHNICAL", 78);
        seedQuestion("database_dba", "MySQL 慢查询优化的标准流程是什么？",
                "面向数据库工程师/DBA 的 SQL 优化题",
                "一个查询从几十毫秒变成数秒，你如何判断是否需要加索引或改写 SQL？",
                "先通过慢查询日志、performance_schema 或 APM 找到 SQL、执行频率和耗时分布；用 EXPLAIN/EXPLAIN ANALYZE 查看访问类型、rows、过滤率、索引使用、回表和排序临时表；结合表结构、基数和业务过滤条件设计联合索引，注意最左前缀和选择性；必要时改写 SQL，避免函数作用在索引列、隐式类型转换、大 offset 分页和无界范围扫描；上线前评估写入成本和索引空间，灰度验证耗时、执行计划和锁影响。",
                "MEDIUM", "TECHNICAL", 76);
    }

    private void seedQuestion(String roleCode, String title, String description, String questionText, String answerText,
                              String difficulty, String questionType, int frequency) {
        jdbcTemplate.update("""
                INSERT INTO questions
                    (title, slug, description, question_text, answer_text, difficulty, category_id, question_type,
                     primary_job_role_id, skill_dimension_summary, standard_answer_points, common_mistakes,
                     follow_up_prompts, scoring_points, recommended_resources, is_for_interview, is_for_practice,
                     interview_frequency, source_type, mark_count, share_count, browse_count, view_count, key_points,
                     related_questions, metadata, source, submit_count, accept_count, accept_rate, is_visible,
                     sort_order, created_by, created_at, updated_at)
                SELECT ?, ?, ?, ?, ?, ?, NULL, ?,
                       jr.id, ?, ?, ?, ?, ?, ?, TRUE, TRUE,
                       ?, 'SYSTEM_SEED', 0, 0, 0, 0, ?, '[]', ?, 'ADMIN', 0, 0, 0.00, TRUE,
                       ?, NULL, NOW(), NOW()
                FROM job_roles jr
                WHERE jr.code = ?
                  AND NOT EXISTS (SELECT 1 FROM questions q WHERE q.slug = ? LIMIT 1)
                LIMIT 1
                """,
                title,
                "seed-" + roleCode + "-" + Math.abs(title.hashCode()),
                description,
                questionText,
                answerText,
                difficulty,
                questionType,
                "技术深度、问题分析、项目表达",
                jsonArray("结论明确", "步骤完整", "结合真实项目", "说明验证方式"),
                jsonArray("只说概念不说落地", "缺少排查顺序", "没有说明验证指标"),
                jsonArray("如果线上还在持续报错，你会先做什么止损？", "你如何证明优化真的有效？"),
                jsonArray("原理准确", "流程清晰", "工程可落地", "风险意识"),
                jsonArray("官方文档", "工程实践复盘", "性能与稳定性案例"),
                frequency,
                jsonArray("岗位化", "真实场景", "参考答案"),
                "{\"source\":\"2026-hot-computer-role-seed\"}",
                frequency,
                roleCode,
                "seed-" + roleCode + "-" + Math.abs(title.hashCode()));
    }

    private String jsonArray(String... values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('"').append(values[i].replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return builder.append(']').toString();
    }

    private void ensureEvaluationCalibrationTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS evaluation_standard_answers (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    job_role_id BIGINT NULL,
                    dimension_code VARCHAR(50) NOT NULL,
                    question_text TEXT NOT NULL,
                    standard_answer TEXT NOT NULL,
                    expert_score DECIMAL(5,2) NOT NULL,
                    expert_comment VARCHAR(500) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_eval_std_role (job_role_id),
                    INDEX idx_eval_std_dimension (dimension_code)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS evaluation_calibration (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    interview_id BIGINT NULL,
                    job_role_id BIGINT NULL,
                    source_type VARCHAR(50) NOT NULL,
                    evaluation_run_count INT NOT NULL DEFAULT 1,
                    expected_score DECIMAL(6,3) NULL,
                    ai_score DECIMAL(6,3) NULL,
                    score_variance DECIMAL(10,3) NULL,
                    bias_rate DECIMAL(10,3) NULL,
                    dimension_scores JSON NULL,
                    calibration_status VARCHAR(30) NOT NULL DEFAULT 'PASS',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_eval_calibration_interview (interview_id),
                    INDEX idx_eval_calibration_role (job_role_id),
                    INDEX idx_eval_calibration_status (calibration_status)
                )
                """);
        jdbcTemplate.update("""
                INSERT INTO evaluation_standard_answers
                    (job_role_id, dimension_code, question_text, standard_answer, expert_score, expert_comment)
                SELECT NULL, 'technicalDepth', '请解释一次典型接口性能优化的定位过程',
                    '应覆盖指标采集、慢 SQL 或外部依赖定位、缓存/索引/异步化等优化手段，并说明验证方式。',
                    85.00, '用于校准技术深度维度的标准样本'
                WHERE NOT EXISTS (SELECT 1 FROM evaluation_standard_answers WHERE dimension_code = 'technicalDepth' LIMIT 1)
                """);
    }

    private void ensureColumn(String columnName, String ddl) {
        if (columnExists(TABLE_NAME, columnName)) {
            return;
        }
        jdbcTemplate.execute(ddl);
        log.info("已自动补齐 ai_interviews.{} 字段", columnName);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }
}
