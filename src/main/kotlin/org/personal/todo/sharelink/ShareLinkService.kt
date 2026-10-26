package org.personal.todo.sharelink


/** The four things a share link can point at. The path segment is the public URL prefix. */
enum class ShareEntity(val segment: String) {
    STADIUM("stadiums"),
    STORE("stores"),
    PRODUCT("products"),
    BOOKING("bookings"),
}

/** What the landing page and the chat-app link preview render. */
data class SharePreview(
    val title: String,
    val description: String,
    val imageUrl: String?,
    val entity: ShareEntity,
    val id: String,
)
//
//@Service
//class ShareLinkService(
//    private val stadiumRepository: StadiumRepository,
//    private val storeRepository: StoreRepository,
//    private val productRepository: ProductRepository,
//    private val bookingRepository: BookingRepository,
//) {
//    /** Null for a malformed id or a deleted row; the page then falls back to generic app branding. */
//    fun preview(entity: ShareEntity, rawId: String): SharePreview? {
//        val id = runCatching { rawId.toObjectId() }.getOrNull() ?: return null
//        val arabic = LocaleContextHolder.getLocale().language == "ar"
//
//        return when (entity) {
//            ShareEntity.STADIUM -> stadiumRepository.findById(id).orElse(null)?.let {
//                SharePreview(it.name, it.locationAddress, it.image.toMediaUrl(FileCategory.IMAGES), entity, rawId)
//            }
//
//            ShareEntity.STORE -> storeRepository.findById(id).orElse(null)?.let {
//                SharePreview(it.name, it.locationAddress, it.image.toMediaUrl(FileCategory.IMAGES), entity, rawId)
//            }
//
//            ShareEntity.PRODUCT -> productRepository.findById(id).orElse(null)?.let {
//                SharePreview(
//                    title = if (arabic) it.nameAr else it.nameEn,
//                    description = if (arabic) it.descriptionAr else it.descriptionEn,
//                    imageUrl = it.images.firstOrNull().toMediaUrl(FileCategory.IMAGES),
//                    entity = entity,
//                    id = rawId,
//                )
//            }
//
//            ShareEntity.BOOKING -> bookingRepository.findById(id).orElse(null)?.let {
//                SharePreview(
//                    title = it.stadiumSnapshot.name,
//                    description = it.stadiumSnapshot.locationAddress,
//                    imageUrl = it.stadiumSnapshot.image.toMediaUrl(FileCategory.IMAGES),
//                    entity = entity,
//                    id = rawId,
//                )
//            }
//        }
//    }
//}
