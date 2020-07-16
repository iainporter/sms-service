package com.porterhead.sms.event

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.junit.jupiter.api.Test
import org.locationtech.jts.util.Assert

class EventDeserialize {

    @Test
    fun parse() {
        val event = """{"schema":{"type":"string","optional":false},"payload":"{\"id\":\"b25edc5d-7ee0-4535-b096-4497cfc8d44c\",\"fromNumber\":\"+1234567890\",\"toNumber\":\"+1234567891\",\"text\":\"Foo Bar!\",\"status\":\"WAITING\"}"}"""
//        val event = """
//            "{\"id\":\"645188cf-5719-4d14-9fb2-a8946f479a7d\",\"fromNumber\":\"+1234567890\",\"toNumber\":\"+1234567899\",\"text\":\"Foo Bar\",\"status\":\"WAITING\"}"
//        """
//        val event2 = """
//            "{"schema":{"type":"string","optional":false},"payload":"{"id":"b06c8f8e-70ff-42b2-b544-ebc6dfb3eed4","fromNumber":"+1234567890","toNumber":"+1234567891","text":"Foo Bar!","status":"WAITING"}"}"
//        """

//        val unescaped = event.replace("\\\"","\"").trimIndent()
//        val trimmed = unescaped.removeSurrounding("\"")
//
//        val objectMapper = ObjectMapper()
//        val eventPayload = objectMapper.readTree(trimmed)

        val json: JsonObject = GsonBuilder().create().fromJson(event, JsonObject::class.java)
        val payload = GsonBuilder().create().fromJson(json.get("payload").asString, JsonObject::class.java)


//        Assert.equals("645188cf-5719-4d14-9fb2-a8946f479a7d", eventPayload.get("id").asText())
    }
}
