package com.porterhead.testing.sms

import com.porterhead.testing.RestFunctions
import com.porterhead.testing.util.BaseComponentTst
import io.restassured.path.json.JsonPath
import org.awaitility.Awaitility
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

class SendMessageTest : BaseComponentTst() {

    @Test
    fun `a valid request to send a SMS Message`() {
        //send a message
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"+1234567899"}"""
        val response = RestFunctions.sendSmsMessage(request)
        val location = response.header("Location")
        //check the message has been processed
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).until {
            val messageResponse = RestFunctions.getMessage(location)
            val json : JsonPath = messageResponse.body.jsonPath()
            json.getString("status") == "DELIVERED"
        }
    }

    @Test
    fun `Message is not found`() {
        RestFunctions.getMessageById(UUID.randomUUID().toString(), 404)
    }
}
