package com.ruoyi.web.core.config;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    /**
     * 是否开启swagger
     */
    @Value("${swaggerConfig.flag}")
    Boolean flag = true;

    /**
     * 后台接口分组
     */
    // @Bean
    public Docket adminDocket() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                // 扫描注解
                .apis(RequestHandlerSelectors.basePackage("com.ruoyi.admin.controller"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 扫描所有.apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .groupName("admin")
                // 设置安全模式，访问token
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 用户业务接口分组
     */
    @Bean
    public Docket userDocket() {
        return new Docket(DocumentationType.OAS_30)
                .enable(flag)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ruoyi.user.controller"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .groupName("user")
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 第三方业务接口分组
     */
    @Bean
    public Docket thirdPartyDocket() {
        return new Docket(DocumentationType.OAS_30)
                .enable(flag)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ruoyi.thirdparty"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .groupName("thirdParty")
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 玩法业务接口分组
     */
    @Bean
    public Docket playingMethodDocket() {
        return new Docket(DocumentationType.OAS_30)
                .enable(flag)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ruoyi.playingmethod.controller"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .groupName("playingMethod")
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 任务业务接口分组
     */
    @Bean
    public Docket taskDocket() {
        return new Docket(DocumentationType.OAS_30)
                .enable(flag)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ruoyi.task.controller"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .groupName("task")
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 推广后台接口分组
     */
    @Bean
    public Docket promoDocket() {
        return new Docket(DocumentationType.OAS_30)
                .enable(flag)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ruoyi.promo.controller"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .groupName("promo")
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 安全模式，这里指定token通过Authorization请求头传递
     */
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> apiKeyList = new ArrayList<SecurityScheme>();
        apiKeyList.add(new ApiKey("Authorization", "Authorization", In.HEADER.toValue()));
        return apiKeyList;
    }

    /**
     * 安全上下文
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(SecurityContext.builder()
                .securityReferences(defaultAuth())
                .operationSelector(o -> o.requestMappingPattern().matches("/.*"))
                .build());
        return securityContexts;
    }

    /**
     * 默认的安全引用
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }
}
