package org.personal.todo.sharelink

import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils

/** The HTML fallback for a share link: OG tags for chat previews, plus open/download buttons. */
@Component
class ShareLandingPageRenderer(
    private val props: ShareLinkProperties,
) {
    fun render(
        entity: ShareEntity,
        id: String,
        preview: SharePreview?,
        canonicalUrl: String,
    ): String {
        val title = preview?.title ?: DEFAULT_TITLE
        val description = preview?.description?.takeIf { it.isNotBlank() } ?: DEFAULT_DESCRIPTION
        val image = preview?.imageUrl
        // Same path as the https link, so the app parses one shape.
        val deepLink = "${props.appScheme}://${entity.segment}/$id"

        return buildString {
            append("<!doctype html>\n<html lang=\"en\">\n<head>\n")
            append("<meta charset=\"utf-8\">\n")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
            append("<title>").append(esc(title)).append("</title>\n")
            append(meta("description", description))
            append(og("og:type", "website"))
            append(og("og:site_name", DEFAULT_TITLE))
            append(og("og:title", title))
            append(og("og:description", description))
            append(og("og:url", canonicalUrl))
            image?.let {
                append(og("og:image", it))
                append(meta("twitter:image", it))
            }
            append(meta("twitter:card", if (image != null) "summary_large_image" else "summary"))
            append(meta("twitter:title", title))
            append(meta("twitter:description", description))
            // iOS Smart App Banner: Safari offers Open/View with no JS.
            props.iosAppIds.firstOrNull()?.substringAfter('.')?.let {
                append("<meta name=\"apple-itunes-app\" content=\"app-id=").append(esc(it))
                    .append(", app-argument=").append(esc(canonicalUrl)).append("\">\n")
            }
            append(STYLE)
            append("</head>\n<body>\n")
            append("<main class=\"card\">\n")
            image?.let { append("<img class=\"hero\" src=\"").append(esc(it)).append("\" alt=\"\">\n") }
            append("<h1>").append(esc(title)).append("</h1>\n")
            append("<p>").append(esc(description)).append("</p>\n")
            append("<a class=\"btn primary\" href=\"").append(esc(deepLink)).append("\">Open in the app</a>\n")
            if (props.appStoreUrl.isNotBlank()) {
                append("<a class=\"btn\" href=\"").append(esc(props.appStoreUrl)).append("\">Download for iPhone</a>\n")
            }
            if (props.playStoreUrl.isNotBlank()) {
                append("<a class=\"btn\" href=\"").append(esc(props.playStoreUrl)).append("\">Download for Android</a>\n")
            }
            append("</main>\n")
            append("</body>\n</html>")
        }
    }

    private fun meta(name: String, content: String) =
        "<meta name=\"${esc(name)}\" content=\"${esc(content)}\">\n"

    private fun og(property: String, content: String) =
        "<meta property=\"${esc(property)}\" content=\"${esc(content)}\">\n"

    private fun esc(value: String) = HtmlUtils.htmlEscape(value)

    private companion object {
        const val DEFAULT_TITLE = "todo"
        const val DEFAULT_DESCRIPTION = "Open this link in the todo app."

        val STYLE = """
            <style>
              :root { color-scheme: light dark; }
              body { margin:0; min-height:100vh; display:flex; align-items:center; justify-content:center;
                     font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;
                     background:#f5f5f7; color:#111; padding:24px; }
              .card { max-width:420px; width:100%; background:#fff; border-radius:16px; padding:24px;
                      box-shadow:0 8px 32px rgba(0,0,0,.08); text-align:center; }
              .hero { width:100%; height:200px; object-fit:cover; border-radius:12px; margin-bottom:16px; }
              h1 { font-size:22px; margin:0 0 8px; }
              p { color:#666; margin:0 0 20px; font-size:15px; }
              .btn { display:block; padding:13px; border-radius:10px; text-decoration:none;
                     margin-top:10px; font-weight:600; background:#eee; color:#111; }
              .btn.primary { background:#111; color:#fff; }
              @media (prefers-color-scheme: dark) {
                body { background:#111; color:#f5f5f7; }
                .card { background:#1c1c1e; box-shadow:none; }
                p { color:#9a9a9e; }
                .btn { background:#2c2c2e; color:#f5f5f7; }
                .btn.primary { background:#f5f5f7; color:#111; }
              }
            </style>

        """.trimIndent()
    }
}
