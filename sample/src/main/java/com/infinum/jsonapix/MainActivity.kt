package com.infinum.jsonapix

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val jsonApiString = Person(
            "Stef",
            "Banek"
        ).toJsonApiString()

        text.text = jsonApiString

       textDecoded.text = jsonApiString.decodeJsonApiString<Person>().name
    }
}





