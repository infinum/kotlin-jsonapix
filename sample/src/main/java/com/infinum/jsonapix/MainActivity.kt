package com.infinum.jsonapix

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.infinum.jsonapix.core.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val json = Json.encodeToString(JsonApiSerializer(DummyData.serializer()), DummyData("Stef"))
        text.text = json

        textDecoded.text = Json.decodeFromString(JsonApiSerializer(DummyData.serializer()), json).name
        DummyData.serializer()
    }
}

@Serializable(with = JsonApiSerializer::class)
data class DummyData(
    @SerialName("name") val name: String
): JsonApiSerializable {
    override fun toString(): String {
        return "Dummy($name)"
    }

    override val dataType: String
        get() = "overridenDummy"
}

@Serializable//(with = JsonApiSerializer::class)
data class TrySerializable<out T>(
    @SerialName("type") val type: String,
    @SerialName("data") val data: T
)