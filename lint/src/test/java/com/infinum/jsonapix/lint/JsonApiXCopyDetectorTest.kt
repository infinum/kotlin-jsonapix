package com.infinum.jsonapix.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class JsonApiXCopyDetectorTest {

    private val annotation = kotlin(
        """
        package com.infinum.jsonapix.annotations

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class JsonApiX(val type: String, val isNullable: Boolean = false)
        """,
    ).indented()

    private val jsonApiModel = kotlin(
        """
        package com.infinum.jsonapix.core

        abstract class JsonApiModel {
            private var type: String? = null
            private var id: String? = null
            fun setType(type: String?) { this.type = type }
            fun setId(id: String?) { this.id = id }
            fun type(): String? = type
            fun id(): String? = id
        }
        """,
    ).indented()

    private val models = kotlin(
        """
        package com.infinum.jsonapix.data

        import com.infinum.jsonapix.annotations.JsonApiX
        import com.infinum.jsonapix.core.JsonApiModel

        @JsonApiX("dog")
        data class Dog(val name: String) : JsonApiModel()

        // A generated wrapper: it holds a model but carries no identity of its own.
        data class DogModel(val data: Dog)

        // Nothing to do with JsonApiX.
        data class Unrelated(val name: String)

        fun Dog.copyModel(name: String = this.name): Dog = Dog(name = name)
        """,
    ).indented()

    // The receivers matter: the fix rewrites text, so one of them has to contain the word the fix
    // searches for. With a receiver of `dog` a whole-call replacement and a name-scoped one give
    // the same answer, which is how a broken fix passed review.
    @ParameterizedTest(name = "receiver {0}")
    @ValueSource(strings = ["dog", "copyOfDog"])
    fun `given copy on a JsonApiModel should report and offer a fix that rewrites only the call`(receiver: String) {
        lint()
            .files(
                annotation,
                jsonApiModel,
                models,
                kotlin(
                    """
                    package com.infinum.jsonapix.usage

                    import com.infinum.jsonapix.data.Dog

                    fun rename($receiver: Dog) = $receiver.copy(name = "Rex")
                    """,
                ).indented(),
            )
            .issues(JsonApiXCopyDetector.ISSUE)
            .run()
            .expectWarningCount(1)
            // Lint renders the message with the backticks stripped.
            .expectContains("copy drops the JSON:API type and id")
            .expectContains("JsonApiXCopyLosesIdentity")
            .expectFixDiffs(
                """
                Fix for src/com/infinum/jsonapix/usage/test.kt line 5: Replace with copyModel:
                @@ -5 +5
                - fun rename($receiver: Dog) = $receiver.copy(name = "Rex")
                + fun rename($receiver: Dog) = $receiver.copyModel(name = "Rex")
                """.trimIndent(),
            )
    }

    @Test
    fun `given copyModel on a JsonApiModel should not report`() {
        lint()
            .files(
                annotation,
                jsonApiModel,
                models,
                kotlin(
                    """
                    package com.infinum.jsonapix.usage

                    import com.infinum.jsonapix.data.Dog
                    import com.infinum.jsonapix.data.copyModel

                    fun rename(dog: Dog) = dog.copyModel(name = "Rex")
                    """,
                ).indented(),
            )
            .issues(JsonApiXCopyDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `given copy on a generated wrapper should not report`() {
        lint()
            .files(
                annotation,
                jsonApiModel,
                models,
                kotlin(
                    """
                    package com.infinum.jsonapix.usage

                    import com.infinum.jsonapix.data.Dog
                    import com.infinum.jsonapix.data.DogModel

                    fun swap(model: DogModel, dog: Dog) = model.copy(data = dog)
                    """,
                ).indented(),
            )
            .issues(JsonApiXCopyDetector.ISSUE)
            .run()
            // A wrapper has no type or id of its own, so copying one loses nothing.
            .expectClean()
    }

    @Test
    fun `given a method whose name merely contains copy should not report`() {
        lint()
            .files(
                annotation,
                jsonApiModel,
                kotlin(
                    """
                    package com.infinum.jsonapix.named

                    import com.infinum.jsonapix.annotations.JsonApiX
                    import com.infinum.jsonapix.core.JsonApiModel

                    @JsonApiX("dog")
                    data class Dog(val name: String) : JsonApiModel() {
                        fun copyOf(): Dog = Dog(name)
                        fun copies(): Int = 1
                        fun recopy(): Dog = Dog(name)
                    }
                    """,
                ).indented(),
                kotlin(
                    """
                    package com.infinum.jsonapix.usage

                    import com.infinum.jsonapix.named.Dog

                    fun a(dog: Dog) = dog.copyOf()
                    fun b(dog: Dog) = dog.copies()
                    fun c(dog: Dog) = dog.recopy()
                    """,
                ).indented(),
            )
            .issues(JsonApiXCopyDetector.ISSUE)
            .run()
            // The name is matched whole, not as a prefix. Switching to something like
            // methodName.startsWith("copy") would report all three of these.
            .expectClean()
    }

    @Test
    fun `given copy on an unrelated data class should not report`() {
        lint()
            .files(
                annotation,
                jsonApiModel,
                models,
                kotlin(
                    """
                    package com.infinum.jsonapix.usage

                    import com.infinum.jsonapix.data.Unrelated

                    fun rename(it: Unrelated) = it.copy(name = "Rex")
                    """,
                ).indented(),
            )
            .issues(JsonApiXCopyDetector.ISSUE)
            .run()
            .expectClean()
    }
}
