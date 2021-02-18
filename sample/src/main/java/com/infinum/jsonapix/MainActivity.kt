package com.infinum.jsonapix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.jsonapix.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        val jsonApiString = Person(
            "Stef",
            "Banek"
        ).toJsonApiString()

        binding.text.text = jsonApiString

        binding.textDecoded.text =
            jsonApiString.decodeJsonApiString<Person>()?.name
    }
}
