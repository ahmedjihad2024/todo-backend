package org.personal.todo.common.extentions

import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import java.util.Locale

// Captures the application's MessageSource once Spring instantiates it, so the
// top-level String.tr() extension below can resolve i18n keys without every
// caller having to inject MessageSource.
@Component
class TranslationContext(messageSource: MessageSource) {

    init {
        source = messageSource
    }

    companion object {
        private lateinit var source: MessageSource

        internal fun resolve(key: String, locale: Locale, args: Array<out Any>): String =
            try {
                source.getMessage(key, args, locale)
            } catch (_: NoSuchMessageException) {
                key
            }
    }
}

fun String.tr(): String = TranslationContext.resolve(this, LocaleContextHolder.getLocale(), emptyArray())

/** Resolves in an explicit locale — for text stored in every language at once, like notifications. */
fun String.tr(locale: Locale, vararg args: Any): String = TranslationContext.resolve(this, locale, args)
