package com.infinum.jsonapix.ui.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.infinum.jsonapix.R

class LoaderView(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_loader)
        window?.setBackgroundDrawableResource(R.color.transparent)
        setCancelable(true)
    }
}
