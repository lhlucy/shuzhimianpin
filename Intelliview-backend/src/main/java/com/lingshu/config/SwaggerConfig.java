package com.lingshu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public Docket authApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("认证模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/auth/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("用户模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/users/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket questionApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("问题模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/questions/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket answerApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("回答模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/answers/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket commentApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("评论模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/comments/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket likeApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("点赞模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/likes/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket favoriteApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("收藏模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/favorites/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket searchApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("搜索模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/search/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket reportApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("举报模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/reports/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("管理员模块")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lingshu.controller"))
                .paths(PathSelectors.ant("/api/admin/**"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Intelliview API")
                .description("Intelliview 后端 API 接口文档")
                .version("1.0.0")
                .contact(new Contact("Intelliview Team", "https://www.intelliview.ai", "contact@intelliview.ai"))
                .build();
    }
}
