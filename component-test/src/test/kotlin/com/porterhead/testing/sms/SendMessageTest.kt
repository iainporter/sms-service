package com.porterhead.testing.sms

import io.restassured.path.json.JsonPath
import org.awaitility.Awaitility
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

class SendMessageTest : BaseComponentTst() {

    /** This number will be mapped to success for all providers*/
    private val successToNumberAll = "+1111111111"

    /** This number will be mapped to success for clicksend but not Twilio*/
    private val successToNumberClicksend = "+2222222222"

    /** This number will be mapped to success for twilio but not ClickSend*/
    private val successToNumberTwilio = "+3333333333"

    /** This number will be mapped to failure for twilio and ClickSend*/
    private val failureToNumberAll = "+4444444444"

    @Test
    fun `a valid request to send a SMS Message`() {
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$successToNumberAll"}"""
        sendMessageAndAssertStatus(request, "DELIVERED")
    }

    @Test
    fun `Message is still sent when twilio returns a server exception`() {
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$successToNumberClicksend"}"""
        sendMessageAndAssertStatus(request, "DELIVERED")
    }

    @Test
    fun `Message is still sent when clicksend returns a server exception`() {
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$successToNumberTwilio"}"""
        sendMessageAndAssertStatus(request, "DELIVERED")
    }

    @Test
    fun `Message is failed when clicksend and twilio returns a server exception`() {
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"$failureToNumberAll"}"""
        sendMessageAndAssertStatus(request, "FAILED")
    }

    @Test
    fun `Message is not found`() {
        restFunctions.getMessageById(UUID.randomUUID().toString(), 404)
    }

    private fun sendMessageAndAssertStatus(request: String, status: String) {
        val response = restFunctions.sendSmsMessage(request)
        val location = response.header("Location")
        //check the message has been processed
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(500, MILLISECONDS).until {
            val messageResponse = restFunctions.getMessage(location)
            val json : JsonPath = messageResponse.body.jsonPath()
            json.getString("status") == status
        }
    }
}
