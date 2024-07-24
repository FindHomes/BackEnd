package com.findhomes.findhomesbe.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String API_NAME = "Kustaurant Mobile Application API";
    private static final String API_VERSION = "v1.0.1";
    private static final String API_DESCRIPTION = """
    찾아줘!홈즈 모바일 앱 API 문서입니다.

    **Version 1.0.0 (2024-07-20)**
    - 매물 검색을 위한 조건 입력 API 구성
    """;


    @Bean
    public OpenAPI OpenAPIConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title(API_NAME)
                        .description(API_DESCRIPTION)
                        .version(API_VERSION));
    }
}
