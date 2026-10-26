//package org.personal.todo.sharelink.controller
//
//import io.swagger.v3.oas.annotations.Hidden
//import jakarta.servlet.http.HttpServletRequest
//import org.personal.todo.sharelink.ShareEntity
//import org.personal.todo.sharelink.ShareLandingPageRenderer
//import org.personal.todo.sharelink.ShareLinkService
//import org.springframework.http.CacheControl
//import org.springframework.http.MediaType
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RestController
//import java.util.concurrent.TimeUnit
//
///** Public share links; only reached when the OS didn't hand the URL to an installed app. */
//@Hidden
//@RestController
//class ShareLinkController(
////    private val shareLinkService: ShareLinkService,
//    private val renderer: ShareLandingPageRenderer,
//) {
//    @GetMapping("/stadiums/{id}", produces = [MediaType.TEXT_HTML_VALUE])
//    fun stadium(@PathVariable id: String, request: HttpServletRequest) =
//        page(ShareEntity.STADIUM, id, request)
//
//    @GetMapping("/stores/{id}", produces = [MediaType.TEXT_HTML_VALUE])
//    fun store(@PathVariable id: String, request: HttpServletRequest) =
//        page(ShareEntity.STORE, id, request)
//
//    @GetMapping("/products/{id}", produces = [MediaType.TEXT_HTML_VALUE])
//    fun product(@PathVariable id: String, request: HttpServletRequest) =
//        page(ShareEntity.PRODUCT, id, request)
//
//    @GetMapping("/bookings/{id}", produces = [MediaType.TEXT_HTML_VALUE])
//    fun booking(@PathVariable id: String, request: HttpServletRequest) =
//        page(ShareEntity.BOOKING, id, request)
//
//    private fun page(
//        entity: ShareEntity,
//        id: String,
//        request: HttpServletRequest,
//    ): ResponseEntity<String> {
//        val preview = shareLinkService.preview(entity, id)
//        val html = renderer.render(
//            entity = entity,
//            id = id,
//            preview = preview,
//            canonicalUrl = request.requestURL.toString(),
//        )
//        // Always 200: a 404 makes chat apps drop the link preview entirely.
//        return ResponseEntity.ok()
//            .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
//            .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
//            .body(html)
//    }
//}
