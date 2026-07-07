package com.board.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodeLog Board API")
                        .description("Spring Boot 기술 블로그 게시판 REST API\n\n" +
                                "**인증 방법**: `/auth/login` 폼 로그인 후 같은 브라우저에서 세션 쿠키로 인증됩니다.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("heo-hyuk")
                                .url("https://github.com/heo-hyuk/board-project")));
    }
}
