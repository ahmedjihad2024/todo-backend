package org.personal.todo.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.slf4j.LoggerFactory
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import java.util.Locale
import java.util.Locale.getDefault

// Wires the OpenAPI spec served by springdoc at /api-docs (path set via
// springdoc.api-docs.path in application.yaml; springdoc's default is /v3/api-docs)
// and the Swagger UI at /swagger-ui.html. Declares a global "bearerAuth" JWT scheme
// so generated Postman collections and Swagger UI know how to attach the Authorization header.
//- Swagger UI: http://localhost:8181/swagger-ui.html — browse and try endpoints
//- Postman import: Import → Link → http://localhost:8181/api-docs → full collection grouped by
//
// Note on Swagger UI responses: the "Response content type" dropdown in the
// "Try it out" panel does NOT auto-select application/json — pick it manually
// before sending the request, otherwise the response body is rendered as plain
// text instead of formatted JSON.
@Configuration
class OpenApiConfig(
    private val environment: Environment,
    @Value($$"${spring.application.name:app}") private val appName: String,
    @Value($$"${server.port:8080}") private val serverPort: String,
    @Value($$"${server.servlet.context-path:}") private val contextPath: String,
    @Value($$"${springdoc.swagger-ui.path:/swagger-ui.html}") private val swaggerUiPath: String,
    @Value($$"${springdoc.api-docs.path:/api-docs}") private val apiDocsPath: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun printApiLinks() {
        val port = environment.getProperty("local.server.port") ?: serverPort
        val base = "http://localhost:$port${contextPath.trimEnd('/')}"
        val swaggerUrl = "$base$swaggerUiPath"
        val apiDocsUrl = "$base$apiDocsPath"
        // Custom page that auto-authorizes once any endpoint returns an accessToken.
        val docsUrl = "$base/docs.html"
        log.info(
            "\n" +
                "================================================================\n" +
                " API ready\n" +
                "   Swagger UI : {}\n" +
                "   Docs (auto-auth) : {}\n" +
                "   Postman    : Import → Link → {}\n" +
                "================================================================",
            swaggerUrl,
            docsUrl,
            apiDocsUrl,
        )
    }

    @Bean
    fun openAPI(): OpenAPI {
        val schemeName = "bearerAuth"
        return OpenAPI()
            .info(
                Info()
                    .title("${appName.uppercase()} API")
                    .description("${appName.uppercase()} API with JWT auth, i18n, and token revocation")
                    .version("0.0.1-SNAPSHOT"),
            )
            .addSecurityItem(SecurityRequirement().addList(schemeName))
            .components(
                Components().addSecuritySchemes(
                    schemeName,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"),
                ),
            )
    }

    // Put the persona tag first so Postman uses it as the top-level folder (springdoc emits the
    // feature tag first). No @Operation summaries are set, so Postman names each request after the
    // camelCase operationId (e.g. "createPost", shown as "create Post"). Set a Title Case summary
    // derived from that operationId so requests read "Create Post".
    @Bean
    fun personaTagOrderCustomizer(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        val personas = listOf("User", "Common")
        openApi.paths?.values?.forEach { pathItem ->
            pathItem.readOperations().forEach { operation ->
                operation.tags = operation.tags?.sortedBy { tag ->
                    personas.indexOf(tag).takeIf { it >= 0 } ?: personas.size
                }
                if (operation.summary.isNullOrBlank()) {
                    operation.operationId?.let { operation.summary = titleCase(it) }
                }
            }
        }
    }

    // "createPost" / "create Post" -> "Create Post": split on camelCase boundaries and whitespace,
    // then uppercase the first letter of every word.
    private fun titleCase(text: String): String =
        text.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
            }
}
