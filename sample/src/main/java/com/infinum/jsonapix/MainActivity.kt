package com.infinum.jsonapix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.jsonapix.Container.extractClassDiscriminator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.json.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val jsonApiString = Person(
            "Stef",
            "Banek"
        ).toJsonApiString().extractClassDiscriminator("#class")

        text.text = jsonApiString

        val personJson =
            """{"data":{"#class":"person","attributes":{"name":"Stef","surname":"Banek"},"id":"0","type":"person"},"errors":null}"""

//        text.text =
//            personJson.injectDiscriminator("#class", "person")?.decodeJsonApiString<Person>()?.name
    }
}

object Container {

    fun findType(input: String): String? {
        try {
            val element = Json.parseToJsonElement(input)
            val data = element.jsonObject["data"]
            val type = data?.jsonObject?.get("type")
            return type?.jsonPrimitive?.content
        } catch (e: Exception) {
            return try {
                val element = Json.parseToJsonElement(input)
                val data = element.jsonObject["data"]
                val first = data?.jsonArray?.get(0)
                val type = first?.jsonObject?.get("type")
                type?.jsonPrimitive?.content
            } catch (e: Exception) {
                null
            }
        }
    }

    fun String.injectDiscriminator(
        discriminatorName: String,
        discriminatorValue: String
    ): String {
        val element = Json.parseToJsonElement(this)
        val entries = element.jsonObject.entries.toMutableSet()
        val entry = object : Map.Entry<String, JsonElement> {
            override val key = discriminatorName
            override val value = JsonPrimitive(discriminatorValue)
        }
        entries.add(entry)
        val resultMap = mutableMapOf<String, JsonElement>()
        resultMap.putAll(entries.map { Pair(it.key, it.value) })
        val newJson = JsonObject(resultMap)
        return newJson.toString()
    }

    fun String.extractClassDiscriminator(
        discriminatorName: String
    ): String {
        val element = Json.parseToJsonElement(this)
        val entries = element.jsonObject.entries.toMutableSet()
        entries.removeAll { it.key == discriminatorName }
        val resultMap = mutableMapOf<String, JsonElement>()
        resultMap.putAll(entries.map { Pair(it.key, it.value) })
        val dataObject = element.jsonObject["data"]
        if (dataObject != null) {
            val dataEntries = dataObject.jsonObject.entries.toMutableSet()
            dataEntries.removeAll { it.key == discriminatorName }
            resultMap["data"] = JsonObject(mapOf(*dataEntries.map { Pair(it.key, it.value) }.toTypedArray()))
        }
        val newJson = JsonObject(resultMap)
        return newJson.toString()
    }
}