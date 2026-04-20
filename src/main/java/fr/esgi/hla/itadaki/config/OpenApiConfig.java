package fr.esgi.hla.itadaki.config;

import org.springframework.context.annotation.Configuration;

/**
 * TODO: SpringDoc OpenAPI configuration.
 *       - Define OpenAPI bean with API info (title, description, version)
 *       - Configure Bearer JWT security scheme so Swagger UI shows Authorize button
 *       - Apply security requirement globally or per-endpoint as needed
 *
 *       Depends on: springdoc-openapi-starter-webmvc-ui (already in pom.xml)
 */
@Configuration
public class OpenApiConfig {

    // TODO: @Bean OpenAPI customOpenAPI()
    //       - Add Info (title="Itadaki API", version="1.0", description=...)
    //       - Add SecurityScheme (type=HTTP, scheme=bearer, bearerFormat=JWT)
    //       - Add SecurityRequirement referencing the scheme above
}
