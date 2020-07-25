package com.porterhead.sms.event

import com.porterhead.sms.domain.MessageStatus
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.jpa.MessageRepository
import com.porterhead.sms.provider.RandomProviderRouter
import com.porterhead.sms.resource.GetSmsMessageResourceTest
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class SmsMessageCreatedHandlerTest {

    @MockK
    val eventLog = mockk<EventLog>()

    @MockK
    val messageRepository = mockk<MessageRepository>()

    @MockK
    val router = mockk<RandomProviderRouter>()

    lateinit var handler: SmsMessageCreatedHandler


    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        handler = SmsMessageCreatedHandler()
        handler.eventLog = eventLog
        handler.messageRepository = messageRepository
        handler.router = router
    }

    @Test
    fun `Message created event is handled`() {
        val messageDouble = messageDouble()
        every { eventLog.alreadyProcessed(any()) } returns false
        val slot = slot<SmsMessage>()
        every { messageRepository.findById(any()) } returns messageDouble
        every { messageRepository.persist(capture(slot)) } answers {slot.captured}
        every { router.routeMessage(any()) } returns messageDouble.apply { messageDouble.status = MessageStatus.DELIVERED }
        handler.onEvent(UUID.randomUUID(), "sms_message_created", "sms_message", eventPayload(messageDouble), Instant.now())
        assertEquals(MessageStatus.DELIVERED, slot.captured.status)
    }

    @Test
    fun `Message created event with schema wrapper is handled`() {
        val messageDouble = messageDouble()
        every { eventLog.alreadyProcessed(any()) } returns false
        val slot = slot<SmsMessage>()
        every { messageRepository.findById(any()) } returns messageDouble
        every { messageRepository.persist(capture(slot)) } answers {slot.captured}
        every { router.routeMessage(any()) } returns messageDouble.apply { messageDouble.status = MessageStatus.DELIVERED }
        val message = """
            {"schema":{"type":"string","optional":false},"payload":"{\"id\":\"b06c8f8e-70ff-42b2-b544-ebc6dfb3eed4\",\"fromNumber\":\"+1234567890\",\"toNumber\":\"+1234567891\",\"text\":\"Foo Bar!\",\"status\":\"WAITING\"}"}            """
        handler.onEvent(UUID.randomUUID(), "sms_message_created", "sms_message", message, Instant.now())
        assertEquals(MessageStatus.DELIVERED, slot.captured.status)
    }

    @Test
    fun `Message has already been processed`() {
        every { eventLog.alreadyProcessed(any()) } returns true
        handler.onEvent(UUID.randomUUID(), "sms_message_created", "sms_message", eventPayload(messageDouble()), Instant.now())
        verify { messageRepository wasNot Called}
    }

    private fun messageDouble(): SmsMessage {
        return SmsMessage(id = GetSmsMessageResourceTest.id,
                fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }

    private fun eventPayload(smsMessage: SmsMessage): String {
        return  """
        "{\"id\":\"${smsMessage.id.toString()}\",\"fromNumber\":\"${smsMessage.fromNumber}\",\"toNumber\":\"${smsMessage.toNumber}\",\"text\":\"${smsMessage.text}\",\"status\":\"WAITING\"}"
        """
    }
}
