package com.infinum.jsonapix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.jsonapix.databinding.ActivityMainBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        val jsonApiString = Person(
//            "Stef",
//            "Banek"
//        ).toJsonApiString()
//
//        binding.text.text = jsonApiString
//
//        binding.textDecoded.text = jsonApiString.decodeJsonApiString<Person>()?.name
        binding.text.text = Json.encodeToString(Sample.NULA)
    }
}

@Serializable
enum class Sample(val number: Int) {
    NULA(1),
    JEDAN(2),
    DVA(3)
}
