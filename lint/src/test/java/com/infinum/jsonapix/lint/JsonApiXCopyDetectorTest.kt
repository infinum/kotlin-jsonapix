package com.infinum.jsonapix.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.jupiter.api.Test

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

    @Test
    fun `given copy on a JsonApiModel should report`() {
        lint()
            .files(
                annotation,
                jsonApiModel,
                models,
                kotlin(
                    """
                    package com.infinum.jsonapix.usage

                    import com.infinum.jsonapix.data.Dog

                    fun rename(dog: Dog) = dog.copy(name = "Rex")
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
                - fun rename(dog: Dog) = dog.copy(name = "Rex")
                + fun rename(dog: Dog) = dog.copyModel(name = "Rex")
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
