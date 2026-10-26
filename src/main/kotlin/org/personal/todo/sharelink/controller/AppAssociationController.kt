package org.personal.todo.sharelink.controller

import io.swagger.v3.oas.annotations.Hidden
import org.personal.todo.sharelink.ShareEntity
import org.personal.todo.sharelink.ShareLinkProperties
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/** Domain-association files the OSes fetch to verify this domain may open the app. */
@Hidden
@RestController
class AppAssociationController(
    private val props: ShareLinkProperties,
) {
    /** iOS Universal Links; `components` is derived from [ShareEntity] so it can't drift. */
    @GetMapping(
        "/.well-known/apple-app-site-association",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun appleAppSiteAssociation(): Map<String, Any> {
        val components = ShareEntity.entries.map { mapOf("/" to "/${it.segment}/*", "comment" to it.segment) }
        return mapOf(
            "applinks" to mapOf(
                "details" to listOf(
                    mapOf(
                        "appIDs" to props.iosAppIds,
                        "components" to components,
                    ),
                ),
            ),
            // Shares the iCloud Keychain password entry with this domain.
            "webcredentials" to mapOf("apps" to props.iosAppIds),
        )
    }

    /** Android App Links. */
    @GetMapping("/.well-known/assetlinks.json", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun assetLinks(): List<Map<String, Any>> = listOf(
        mapOf(
            "relation" to listOf("delegate_permission/common.handle_all_urls"),
            "target" to mapOf(
                "namespace" to "android_app",
                "package_name" to props.androidPackageName,
                "sha256_cert_fingerprints" to props.androidSha256Fingerprints,
            ),
        ),
    )
}
