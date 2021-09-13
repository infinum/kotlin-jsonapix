package com.infinum.jsonapix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.jsonapix.core.resources.ResourceObject
import com.infinum.jsonapix.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dog =
            Person("Stef", "Banek", 27, listOf(Dog("Bella", 2), Dog("Bongo", 7)), Dog("Bella", 2))
        binding.text.text = dog.toJsonXString()

        binding.textDecoded.text = dog.toJsonXString().decodeJsonXString<Person>()?.name
    }

}