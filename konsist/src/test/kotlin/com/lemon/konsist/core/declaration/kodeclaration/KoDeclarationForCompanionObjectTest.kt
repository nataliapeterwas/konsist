package com.lemon.konsist.core.declaration.kodeclaration

import com.lemon.konsist.TestSnippetProvider.getSnippetKoScope
import com.lemon.konsist.core.const.KoModifier
import com.lemon.konsist.core.const.KoModifier.FINAL
import com.lemon.konsist.core.const.KoModifier.PRIVATE
import com.lemon.konsist.core.const.KoModifier.PROTECTED
import com.lemon.konsist.testdata.NonExistingAnnotation
import com.lemon.konsist.testdata.SampleAnnotation
import com.lemon.konsist.testdata.SampleAnnotation1
import com.lemon.konsist.testdata.SampleAnnotation2
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Test

class KoDeclarationForCompanionObjectTest {
    @Test
    fun `companion-object-is-top-level`() {
        // given
        val sut = getSnippetFile("companion-object-is-top-level")
            .companionObjects(includeNested = true)
            .first()

        // then
        sut.isTopLevel() shouldBeEqualTo true
    }

    @Test
    fun `companion-object-is-not-top-level`() {
        // given
        val sut = getSnippetFile("companion-object-is-not-top-level")
            .companionObjects(includeNested = true)
            .first { it.name == "SampleNestedCompanionObject" }

        // then
        sut.isTopLevel() shouldBeEqualTo false
    }

    @Test
    fun `companion-object-has-no-annotation`() {
        // given
        val sut = getSnippetFile("companion-object-has-no-annotation")
            .companionObjects()
            .first()

        // then
        sut.annotations shouldHaveSize 0
    }

    @Test
    fun `companion-object-has-annotation`() {
        // given
        val sut = getSnippetFile("companion-object-has-annotation")
            .companionObjects()
            .first()

        // then
        sut.run {
            annotations shouldHaveSize 1
            hasAnnotation("SampleAnnotation") shouldBeEqualTo true
            hasAnnotation("NonExistingAnnotation") shouldBeEqualTo false
            hasAnnotation("com.lemon.konsist.testdata.SampleAnnotation") shouldBeEqualTo true
            hasAnnotation("com.lemon.konsist.testdata.NonExistingAnnotation") shouldBeEqualTo false
            hasAnnotation<SampleAnnotation>() shouldBeEqualTo true
            hasAnnotation<NonExistingAnnotation>() shouldBeEqualTo false
        }
    }

    @Test
    fun `companion-object-has-two-annotations`() {
        // given
        val sut = getSnippetFile("companion-object-has-two-annotations")
            .companionObjects()
            .first()

        // then
        sut.run {
            annotations shouldHaveSize 2
            hasAnnotation("SampleAnnotation1") shouldBeEqualTo true
            hasAnnotation("SampleAnnotation2") shouldBeEqualTo true
            hasAnnotation("NonExistingAnnotation") shouldBeEqualTo false
            hasAnnotation("com.lemon.konsist.testdata.SampleAnnotation1") shouldBeEqualTo true
            hasAnnotation("com.lemon.konsist.testdata.SampleAnnotation2") shouldBeEqualTo true
            hasAnnotation("com.lemon.konsist.testdata.NonExistingAnnotation") shouldBeEqualTo false
            hasAnnotation<SampleAnnotation1>() shouldBeEqualTo true
            hasAnnotation<SampleAnnotation2>() shouldBeEqualTo true
            hasAnnotation<NonExistingAnnotation>() shouldBeEqualTo false
        }
    }

    @Test
    fun `companion-object-has-no-visibility-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-no-visibility-modifier")
            .companionObjects()
            .first()

        // then
        sut.run {
            isPublicOrDefault() shouldBeEqualTo true
            hasPublicModifier() shouldBeEqualTo false
            hasPrivateModifier() shouldBeEqualTo false
            hasProtectedModifier() shouldBeEqualTo false
            hasInternalModifier() shouldBeEqualTo false
        }
    }

    @Test
    fun `companion-object-has-public-visibility-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-public-visibility-modifier")
            .companionObjects()
            .first()

        // then
        sut.run {
            isPublicOrDefault() shouldBeEqualTo true
            hasPublicModifier() shouldBeEqualTo true
            hasPrivateModifier() shouldBeEqualTo false
            hasProtectedModifier() shouldBeEqualTo false
            hasInternalModifier() shouldBeEqualTo false
        }
    }

    @Test
    fun `companion-object-has-private-visibility-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-private-visibility-modifier")
            .companionObjects()
            .first()

        // then
        sut.run {
            isPublicOrDefault() shouldBeEqualTo false
            hasPublicModifier() shouldBeEqualTo false
            hasPrivateModifier() shouldBeEqualTo true
            hasProtectedModifier() shouldBeEqualTo false
            hasInternalModifier() shouldBeEqualTo false
        }
    }

    @Test
    fun `companion-object-has-protected-visibility-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-protected-visibility-modifier")
            .companionObjects()
            .first()

        // then
        sut.run {
            isPublicOrDefault() shouldBeEqualTo false
            hasPublicModifier() shouldBeEqualTo false
            hasPrivateModifier() shouldBeEqualTo false
            hasProtectedModifier() shouldBeEqualTo true
            hasInternalModifier() shouldBeEqualTo false
        }
    }

    @Test
    fun `companion-object-has-internal-visibility-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-internal-visibility-modifier")
            .companionObjects()
            .first()

        // then
        sut.run {
            isPublicOrDefault() shouldBeEqualTo false
            hasPublicModifier() shouldBeEqualTo false
            hasPrivateModifier() shouldBeEqualTo false
            hasProtectedModifier() shouldBeEqualTo false
            hasInternalModifier() shouldBeEqualTo true
        }
    }

    @Test
    fun `companion-object-has-fully-qualified-name`() {
        // given
        val sut = getSnippetFile("companion-object-has-fully-qualified-name")
            .companionObjects()
            .first()

        // then
        sut.fullyQualifiedName shouldBeEqualTo "com.samplepackage.SampleCompanionObject"
    }

    @Test
    fun `companion-object-is-in-package`() {
        // given
        val sut = getSnippetFile("companion-object-is-in-package")
            .companionObjects()
            .first()

        // then
        sut.packageName shouldBeEqualTo "com.samplepackage"
    }

    @Test
    fun `companion-object-is-not-in-package`() {
        // given
        val sut = getSnippetFile("companion-object-is-not-in-package")
            .companionObjects()
            .first()

        // then
        sut.packageName shouldBeEqualTo ""
    }

    @Test
    fun `companion-object-has-protected-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-protected-modifier")
            .companionObjects()
            .first()

        // then
        sut.hasModifiers() shouldBeEqualTo true
    }

    @Test
    fun `companion-object-has-public-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-public-modifier")
            .companionObjects()
            .first()

        // then
        sut.run {
            hasModifiers(KoModifier.PUBLIC) shouldBeEqualTo true
            hasModifiers(PRIVATE) shouldBeEqualTo false
        }
    }

    @Test
    fun `companion-object-has-two-modifiers`() {
        // given
        val sut = getSnippetFile("companion-object-has-two-modifiers")
            .companionObjects()
            .first()

        // then
        sut.run {
            hasModifiers(PROTECTED) shouldBeEqualTo true
            hasModifiers(FINAL) shouldBeEqualTo true
            hasModifiers(PRIVATE) shouldBeEqualTo false
            hasModifiers(PROTECTED, FINAL) shouldBeEqualTo true
            hasModifiers(FINAL, PROTECTED) shouldBeEqualTo true
            hasModifiers(FINAL, PRIVATE) shouldBeEqualTo false
            hasModifiers(PROTECTED, FINAL, PRIVATE) shouldBeEqualTo false
            hasModifiers() shouldBeEqualTo true
        }
    }

    @Test
    fun `companion-object-has-no-modifier`() {
        // given
        val sut = getSnippetFile("companion-object-has-no-modifiers")
            .companionObjects()
            .first()

        // then
        sut.run {
            hasModifiers() shouldBeEqualTo true
            hasModifiers(PRIVATE) shouldBeEqualTo false
        }
    }

    private fun getSnippetFile(fileName: String) = getSnippetKoScope("core/declaration/kodeclaration/snippet/forcompanionobject/", fileName)
}
