package com.lemon.konsist.core.kofunction

import com.lemon.konsist.TestSnippetProvider.getSnippetKoScope
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class KoFunctionTest {
    @Test
    fun `function-with-operator-modifier`() {
        // given
        val sut = getSut("function-with-operator-modifier")
            .functions()
            .first()

        // then
        sut.isOperator shouldBeEqualTo true
    }

    @Test
    fun `function-without-operator-modifier`() {
        // given
        val sut = getSut("function-without-operator-modifier")
            .functions()
            .first()

        // then
        sut.isOperator shouldBeEqualTo false
    }

    @Test
    fun `function-with-inline-modifier`() {
        // given
        val sut = getSut("function-with-inline-modifier")
            .functions()
            .first()

        // then
        sut.isInline shouldBeEqualTo true
    }

    @Test
    fun `function-without-inline-modifier`() {
        // given
        val sut = getSut("function-without-inline-modifier")
            .functions()
            .first()

        // then
        sut.isInline shouldBeEqualTo false
    }

    @Test
    fun `function-with-local-function`() {
        // given
        val sut = getSut("function-with-local-function")
            .functions()
            .first()

        // then
        sut
            .localFunctions()
            .map { it.name } shouldBeEqualTo listOf("sampleLocalFunction")
    }

    @Test
    fun `function-without-local-functions`() {
        // given
        val sut = getSut("function-without-local-functions")
            .functions()
            .first()

        // then
        sut
            .localFunctions()
            .isEmpty() shouldBeEqualTo true
    }

    @Test
    fun `function-with-return-type`() {
        // given
        val sut = getSut("function-with-return-type")
            .functions()
            .first()

        // then
        sut.apply {
            hasExplicitReturnType shouldBeEqualTo true
            getExplicitReturnType shouldBeEqualTo "SampleType"
        }
    }

    @Test
    fun `function-without-return-type`() {
        // given
        val sut = getSut("function-without-return-type")
            .functions()
            .first()

        // then
        sut.hasExplicitReturnType shouldBeEqualTo false
    }

    @Test
    fun `function-with-two-parameters-and-return-type`() {
        // given
        val sut = getSut("function-with-two-parameters-and-return-type")
            .functions()
            .first()

        // then
        sut.apply {
            parameters.size shouldBeEqualTo 2
            getExplicitReturnType shouldBeEqualTo "Boolean"
        }
    }

    @Test
    fun `function-with-parameter`() {
        // given
        val sut = getSut("function-with-parameter")
            .functions()
            .first()

        // then
        sut
            .parameters
            .size shouldBeEqualTo 1
    }

    @Test
    fun `function-with-named-parameter`() {
        // given
        val sut =
            getSut("function-with-named-parameter")
                .functions()
                .first()

        // then
        sut.apply {
            hasParameterNamed("sampleParameter") shouldBeEqualTo true
            hasParameterNamed("otherParameter") shouldBeEqualTo false
        }
    }

    @Test
    fun `function-with-local-property includeLocal true`() {
        // given
        val sut = getSut("function-with-local-property")
            .files()
            .first()

        // then
        sut
            .properties(includeLocal = true)
            .map { it.name } shouldBeEqualTo listOf("sampleLocalProperty")
    }

    @Test
    fun `function-with-local-property includeLocal false`() {
        // given
        val sut = getSut("function-with-local-property")
            .files()
            .first()

        // then
        sut
            .properties(includeLocal = false)
            .map { it.name } shouldBeEqualTo emptyList()
    }

    private fun getSut(fileName: String) = getSnippetKoScope("core/kofunction/snippet/$fileName.kt.txt")
}
