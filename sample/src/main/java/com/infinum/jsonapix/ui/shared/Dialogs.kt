package com.infinum.jsonapix.ui.shared

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showMessageDialog(
    context: Context,
    title: String,
    message: String,
    positiveCallback: (() -> Unit)?,
) {
    MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Ok") { _, _ ->
            positiveCallback?.invoke()
        }.show()
}
