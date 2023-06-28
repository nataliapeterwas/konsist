package com.lemonappdev.konsist.circulardependency1

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.core.architecture.Layer
import com.lemonappdev.konsist.core.exception.KoPreconditionFailedException
import org.amshove.kluent.shouldThrow
import org.amshove.kluent.withMessage
import org.junit.jupiter.api.Test

class CircularDependency1Test {
    @Test
    fun `circular dependency 1`() {
        // given
        val layer1 = Layer("layer1", "layer1")
        val layer2 = Layer("layer2", "layer2")

        // when
        val sut = {
            Konsist
                .architecture(layer1, layer2)
                .addDependencies {
                    layer1.dependsOn(layer2)
                    layer2.dependsOn(layer1)
                }
        }

        // then
        sut shouldThrow KoPreconditionFailedException::class withMessage """
            Illegal circular dependencies:
            Layer(name=layer2, isDefinedBy=layer2) -->
            Layer(name=layer1, isDefinedBy=layer1) -->
            Layer(name=layer2, isDefinedBy=layer2).
        """.trimIndent()
    }
}
