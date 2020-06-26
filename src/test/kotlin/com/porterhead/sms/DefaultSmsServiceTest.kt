package com.porterhead.sms

import com.porterhead.api.sms.SendSmsRequest
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class DefaultSmsServiceTest {

    @Inject
    lateinit var smsService: SmsService

    @Test
    fun `A valid message is persisted and retrieved`() {
        var request = getSendSmsRequest()
        val message = smsService.createMessage(request)
        val foundMessage = smsService.getMessage(message.id)
        Assertions.assertEquals(message, foundMessage)
    }


    private fun getSendSmsRequest(): SendSmsRequest {
        var request = SendSmsRequest()
        request.text = "Foo Message"
        request.toNumber = "+10234567890"
        request.fromNumber = "+10987654321"
        return request
    }
}
