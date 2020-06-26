package com.porterhead.sms

import ClickSend.Model.SmsMessage
import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.jpa.MessageRepository
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import javax.inject.Inject

@QuarkusTest
class DefaultSmsServiceTest {

//    @InjectMock
//    lateinit var messageRepository: MessageRepository


    @Inject
    lateinit var smsService: SmsService

    @Test
    fun `A valid message is persisted and retrieved`() {
        var request = getSendSmsRequest()
        val message = smsService.createMessage(request)
        val foundMessage = smsService.getMessage(message.id)
        Assertions.assertEquals(message, foundMessage)
//        // Check that we called it 1 time
//        Mockito.verify(messageRepository, Mockito.atLeastOnce()).persist(message)
    }


    private fun getSendSmsRequest(): SendSmsRequest {
        var request = SendSmsRequest()
        request.text = "Foo Message"
        request.toNumber = "+10234567890"
        request.fromNumber = "+10987654321"
        return request
    }
}
