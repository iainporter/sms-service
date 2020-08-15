package com.porterhead.sms

import com.porterhead.api.sms.PagedMessageResponse
import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.MessageStatus
import com.porterhead.sms.jpa.MessageRepository
import com.porterhead.sms.resource.PageableQuery
import com.porterhead.sms.resource.QueryRequest
import io.quarkus.panache.common.Page
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.transaction.Transactional

@QuarkusTest
class DefaultSmsServiceTest {

    @Inject
    lateinit var smsService: SmsService

    @Inject
    lateinit var messageRepository: MessageRepository

    val principal = "foo"

    @BeforeEach
    @Transactional
    fun setup() {
        messageRepository.deleteAll()
    }

    @Test
    fun `A valid message is persisted and retrieved`() {
        var request = getSendSmsRequest()
        val message = smsService.createMessage(request, principal)
        val foundMessage = smsService.getMessage(message.id)
        assertEquals(message, foundMessage)
    }

    @Test
    fun `paging result set by MessageStatus`() {
        //create 100 messages
        (1..100).forEach{
            smsService.createMessage(getSendSmsRequest(), principal)}
        //get the first 10
        var response = smsService.getMessages(PageableQuery(Page(0, 10), QueryRequest.Builder().status(MessageStatus.WAITING).build()))
        assertResults(response)
        //get the third page
        response = smsService.getMessages(PageableQuery(Page(2, 10), QueryRequest.Builder().status(MessageStatus.WAITING).build()))
        assertEquals(2, response.page?.page)
    }

    @Test
    fun `paging result set by ToNumber`() {
        //create 100 messages
        (1..100).forEach{
            smsService.createMessage(getSendSmsRequest(), principal)}
        //get the first 10
        var response = smsService.getMessages(PageableQuery(Page(0, 10), QueryRequest.Builder().toNumber("+10234567890").build()))
        assertResults(response)
    }

    @Test
    fun `find all messages`() {
        //create 100 messages
        (1..100).forEach{
            smsService.createMessage(getSendSmsRequest(), principal)}
        //get the first 10
        var response = smsService.getMessages(PageableQuery(Page(0, 10), QueryRequest.Builder().build()))
        assertResults(response)
    }

    @Test
    fun `no messages found`() {
        //create 100 messages
        (1..100).forEach{
            smsService.createMessage(getSendSmsRequest(), principal)}
        var response = smsService.getMessages(PageableQuery(Page(0, 10), QueryRequest.Builder().status(MessageStatus.FAILED).build()))
        assertEquals(0, response.page?.numberOfElements)
        assertEquals(10, response.page?.pageSize)
        assertEquals(0, response.page?.page)
        assertEquals(0, response.page?.totalElements)
        assertEquals(1, response.page?.totalPages)    }

    @Test
    fun `paging result set by MessageStatus AND toNumber`() {
        //create 100 messages
        (1..100).forEach{
            smsService.createMessage(getSendSmsRequest(), principal)}
        //get the first 10
        var response = smsService.getMessages(PageableQuery(Page(0, 10), QueryRequest.Builder().status(MessageStatus.WAITING).toNumber("+10234567890").build()))
        assertResults(response)
    }

    @Test
    fun `sort by status` () {
        //create 10 messages
        (1..100).forEach{
            smsService.createMessage(getSendSmsRequest(), principal)
        }
        var response = smsService.getMessages(PageableQuery(Page(0, 10), QueryRequest.Builder().sortString("status:desc").build()))
        assertResults(response)
    }

    @Test
    fun `sort by status and createdAt` () {
        //create 10 messages
        (1..100).forEach{
            smsService.createMessage(getSendSmsRequest(), principal)
        }
        var response = smsService.getMessages(PageableQuery(Page(0, 10), QueryRequest.Builder().sortString("status:desc,createdAt:desc").build()))
        assertResults(response)
    }

    private fun assertResults(response: PagedMessageResponse) {
        assertEquals(10, response.page?.numberOfElements)
        assertEquals(10, response.page?.pageSize)
        assertEquals(0, response.page?.page)
        assertEquals(100, response.page?.totalElements)
        assertEquals(10, response.page?.totalPages)
    }


    private fun getSendSmsRequest(): SendSmsRequest {
        var request = SendSmsRequest()
        request.text = "Foo Message"
        request.toNumber = "+10234567890"
        request.fromNumber = "+10987654321"
        return request
    }
}
