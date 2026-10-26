package org.personal.todo.common.storage

import org.personal.todo.account.enums.Role
import org.personal.todo.common.exception.ApiException
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration


@Hidden
@RestController
class MediaController(
    private val fileStorageService: FileStorageService,
) {
    @Suppress("Xss")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/media/{filename:.+}")
    fun get(
        @PathVariable category: String,
        @PathVariable filename: String,

        @RequestParam(required = false, defaultValue = "100")
        @Min(20)
        @Max(100)
        quality: Int,

        @RequestParam(required = false, defaultValue = "100")
        @Min(20)
        @Max(100)
        scale: Int,

        @AuthenticationPrincipal currentUserId: String,

    ): ResponseEntity<Resource> {
        val fileCategory = FileCategory.fromSegment(category)
            ?.takeIf { it.visibility == Visibility.PUBLIC }
            ?: throw ApiException.NotFound("error.file_not_found")

        val file = fileStorageService.load(fileCategory, filename)
            ?: throw ApiException.NotFound("error.file_not_found")

        val compressedFile = fileStorageService.compress(
            resource = file.resource,
            quality = quality,
            scale = scale
        )

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .cacheControl(
                CacheControl.maxAge(Duration.ofDays(365)).cachePublic()
            )
            .body(compressedFile)
    }
}
