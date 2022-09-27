package com.infinum.jsonapix.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

private const val MIN_API = 8

@Suppress("UnstableApiUsage")
class JsonApiXIssueRegistry : IssueRegistry() {
    override val issues = listOf(JsonApiXCodeDetector.ANNOTATION_USAGE_ISSUE)

    override val api: Int
        get() = CURRENT_API

    // works with Studio 4.1 or later; see com.android.tools.lint.detector.api.Api / ApiKt
    override val minApi: Int = MIN_API

    override val vendor: Vendor = Vendor(
        vendorName = "Infinum Inc.",
        feedbackUrl = "https://github.com/infinum/kotlin-jsonapix/issues",
        contact = "https://github.com/infinum/kotlin-jsonapix"
    )
}
