package com.lemonappdev.konsist.api.provider

interface KoHasPackageProvider: KoProvider {
    /**
     * Whether the file has package.
     *
     * @param name the name of the package to check.
     * @return `true` if the file has a package with the specified name, `false` otherwise.
     */
    fun hasPackage(name: String): Boolean
}
