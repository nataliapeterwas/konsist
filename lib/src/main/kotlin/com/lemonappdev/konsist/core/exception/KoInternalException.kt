package com.lemonappdev.konsist.core.exception

import com.lemonappdev.konsist.api.container.KoFile
import com.lemonappdev.konsist.api.declaration.KoBaseDeclaration

class KoInternalException(
    message: String,
    cause: Throwable? = null,
    koBaseDeclaration: KoBaseDeclaration? = null,
) : KoException(message.prepare(koBaseDeclaration), cause) {

    constructor(
        message: String,
        cause: Throwable? = null,
        koFile: KoFile?,
    ) : this(message.prepare(koFile), cause)
}

private fun String.prepare(koBaseDeclaration: KoBaseDeclaration?): String {
    return if (koBaseDeclaration == null) {
        this
    } else {
        "$this, declaration:\n${koBaseDeclaration.text}"
    }
}

private fun String.prepare(koFile: KoFile?): String {
    return if (koFile == null) {
        this
    } else {
        "$this, file:\n${koFile.text}"
    }
}
