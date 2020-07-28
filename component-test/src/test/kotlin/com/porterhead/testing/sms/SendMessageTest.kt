package com.porterhead.testing.sms

import com.porterhead.testing.RestFunctions
import com.porterhead.testing.RestFunctions.failureToNumberAll
import com.porterhead.testing.RestFunctions.getMessageById
import com.porterhead.testing.RestFunctions.sendSmsMessage
import com.porterhead.testing.RestFunctions.successToNumberAll
import com.porterhead.testing.RestFunctions.successToNumberClicksend
import com.porterhead.testing.RestFunctions.successToNumberTwilio
import io.restassured.path.json.JsonPath
import org.awaitility.Awaitility
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

class SendMessageTest : BaseComponentTst() {

    @Test
    fun `a valid request to send a SMS Message`() {
        //send a message
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$successToNumberAll"}"""
        sendMessageAndAssertStatus(request, "DELIVERED")
    }

    @Test
    fun `Message is still sent when twilio returns a server exception`() {
        //send a message
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$successToNumberClicksend"}"""
        sendMessageAndAssertStatus(request, "DELIVERED")
    }

    @Test
    fun `Message is still sent when clicksend returns a server exception`() {
        //send a message
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$successToNumberTwilio"}"""
        sendMessageAndAssertStatus(request, "DELIVERED")
    }

    @Test
    fun `Message is failed when clicksend and twilio returns a server exception`() {
        //send a message
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$failureToNumberAll"}"""
        sendMessageAndAssertStatus(request, "FAILED")
    }

    @Test
    fun `Message is not found`() {
        getMessageById(UUID.randomUUID().toString(), 404)
    }

    private fun sendMessageAndAssertStatus(request: String, status: String) {
        val response = sendSmsMessage(request)
        val location = response.header("Location")
        //check the message has been processed
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(500, MILLISECONDS).until {
            val messageResponse = RestFunctions.getMessage(location)
            val json : JsonPath = messageResponse.body.jsonPath()
            json.getString("status") == status
        }
    }
}
