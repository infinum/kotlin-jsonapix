package com.infinum.jsonapix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.jsonapix.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dogs = listOf(Dog("Bella", 2), Dog("Sparky", 4))
        val dogsJsonString = dogs.toJsonApiXString()
        binding.text.text = personListTestJsonString.decodeJsonApiXListString<Person>().toString()
        binding.textDecoded.text = dogsJsonString.decodeJsonApiXListString<Dog>().toString()
    }
}
