package com.gengzi.sftp.usermodel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * 自定义API文档信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API基本信息
                .info(new Info()
                        .title("SFTP to S3 管理界面服务 API 文档") // 文档标题
                        .description("用于管理系统") // 文档描述
                        .version("v1.0.0") // 接口版本
                        // 联系人信息
                        .contact(new Contact()
                                .name("gengzi")
                                .email("gengzi@example.com"))
                        // 许可证信息
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
    