package org.personal.todo.sharelink

import org.springframework.boot.context.properties.ConfigurationProperties

/** What the deep-link layer needs to know about the mobile apps. */
@ConfigurationProperties("app.share-link")
data class ShareLinkProperties(
    /** Full iOS app IDs as `TEAMID.BUNDLEID`, e.g. `ABCDE12345.com.personal.todo`. */
    val iosAppIds: List<String> = emptyList(),
    /** Android application id, e.g. `com.personal.todo`. */
    val androidPackageName: String = "",
    /** Colon-separated uppercase hex; MUST include the Play App Signing key, not just the upload key. */
    // keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android
    val androidSha256Fingerprints: List<String> = emptyList(),
    /** Custom URL scheme backing the "Open in the app" button, e.g. `todo`. */
    val appScheme: String = "",
    val appStoreUrl: String = "",
    val playStoreUrl: String = "",
)
