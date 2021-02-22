package com.infinum.jsonapix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.jsonapix.core.discriminators.CommonDiscriminator
import com.infinum.jsonapix.databinding.ActivityMainBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val jsonApiString = Person(
            "Stef",
            "Banek"
        ).toJsonApiString()

        binding.text.text = jsonApiString

        binding.textDecoded.text = jsonApiString.decodeJsonApiString<Person>()?.name
    }
}
