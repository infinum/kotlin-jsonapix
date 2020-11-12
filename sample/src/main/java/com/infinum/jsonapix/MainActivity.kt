package com.infinum.jsonapix

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


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

@Serializable
sealed class Parent {
    abstract val name: String
}


@Serializable
class Child(override val name: String, val age: Int) : Parent()





