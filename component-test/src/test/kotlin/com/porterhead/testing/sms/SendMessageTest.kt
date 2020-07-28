package com.porterhead.testing.sms

import com.porterhead.testing.RestFunctions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.util.*

class SendMessageTest : BaseTst() {

    @Test
    fun `a valid request to send a SMS Message`() {
        //send a message
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"+1234567899"}"""
        val response = RestFunctions.sendSmsMessage(request)
        val location = response.header("Location")
        //check the message can be retrieved
        val messageResponse = RestFunctions.getMessage(location)
        val json = messageResponse.body.jsonPath()
        assertThat(json.get("id"), `is`(location.substring(location.lastIndexOf("/")+ 1)))
        assertThat(json.get("status"), `is`("WAITING"))
        assertThat(json.get("toNumber"), `is`("+1234567899"))
        assertThat(json.get("fromNumber"), `is`("+1234567890"))
        assertThat(json.get("text"), `is`("Foo Bar"))
    }

    @Test
    fun `Message is not found`() {
        RestFunctions.getMessageById(UUID.randomUUID().toString(), 404)
    }
}
