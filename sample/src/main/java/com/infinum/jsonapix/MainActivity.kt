package com.infinum.jsonapix

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.infinum.jsonapix.core.JsonApiWrapper
import com.infinum.jsonapix.core.resources.ResourceObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.lang.Exception
import kotlin.reflect.full.declaredMembers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val jsonApiString = Person(
            "Stef",
            "Banek"
        ).toJsonApiString()

        text.text = Container.findType("""{ "data": [{ "name": "Stef", "type": "Banek" }] }""")

//        textDecoded.text = jsonApiString.decodeJsonApiString<Person>()?.name
//        textDecoded.text = jsonApiString.replace(
//            "\"#class\":\"com.infinum.jsonapix.JsonApiSerializable_Person\",",
//            ""
//        ).decodeJsonApiString<Person>()?.name
        Container.findType("""{ "data": { "name": "Stef", "type": "Banek" } }""")
        Log.d("tag", "FAFALA SI")
    }
}

inline fun <reified T> getType(): String? {
    val member = T::class.declaredMembers.firstOrNull { it.name == "type" }
    return if (member != null) {
        try {
            member.call() as String
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}

object Container {
    public val jsonApiSerializerModule: SerializersModule = SerializersModule {
        polymorphic(JsonApiWrapper::class) {
            subclass(JsonApiSerializable_Person::class)
            subclass(JsonApiSerializable_Dog::class)
        }
        polymorphic(ResourceObject::class) {
            subclass(ResourceObject_Person::class)
            subclass(ResourceObject_Dog::class)
        }
    }


    public val format: Json = Json {
        encodeDefaults = true
        classDiscriminator = "#class"
        serializersModule = jsonApiSerializerModule
    }

    fun findType(input: String): String? {
        try {
            val element = Json.parseToJsonElement(input)
            val data = element.jsonObject["data"]
            val type = data?.jsonObject?.get("type")
            val result = type?.jsonPrimitive?.content
            return result.toString()
        } catch (e: Exception) {
            return try {
                val element = Json.parseToJsonElement(input)
                val data = element.jsonObject["data"]
                val first = data?.jsonArray?.get(0)
                val type = first?.jsonObject?.get("type")
                type?.jsonPrimitive?.content.toString()
            } catch (e: Exception) {
                null
            }
        }
    }

    fun injectDiscriminator(input: String, discriminatorName: String, discriminatorValue: String): String? {
        try {
            val element = Json.parseToJsonElement(input)
            element.
        } catch (e: Exception) {
            return try {
                val element = Json.parseToJsonElement(input)
                val data = element.jsonObject["data"]
                val first = data?.jsonArray?.get(0)
                val type = first?.jsonObject?.get("type")
                type?.jsonPrimitive?.content.toString()
            } catch (e: Exception) {
                null
            }
        }
    }



    inline fun <reified T> String.decodeFromJsonApi(): T {

    }
}