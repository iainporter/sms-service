package com.porterhead.sms.resource

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import com.porterhead.api.sms.Message
import com.porterhead.api.sms.PagedMessageResponse
import com.porterhead.api.sms.PagedMessageResponsePage
import com.porterhead.sms.SmsService
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import javax.ws.rs.core.Response

@QuarkusTest
class GetMessagesResourceTest {

    @InjectMock
    lateinit var smsService: SmsService

    @Test
    @DisplayName("GET /v1/sms/{id} returns 200")
    fun testGetMessages() {
        whenever(smsService.getMessages(any())).thenReturn(testDouble())
        val response = RestAssured.given()
                .`when`()
                .contentType(ContentType.JSON)
                .get("/v1/sms?status=WAITING&sort=createdAt:desc")
                .then()
                .log().all()
                .statusCode(Response.Status.OK.statusCode)
                .extract()
                .`as`(PagedMessageResponse::class.java)

        assertEquals(response.content?.size, 10)
        assertEquals(response.page?.page, 0)
        assertEquals(response.page?.pageSize, 10)
        assertEquals(response.page?.numberOfElements, 10)
        assertEquals(response.page?.totalElements, 100)
        assertEquals(response.page?.totalPages, 1)

    }

    private fun testDouble(): PagedMessageResponse {
        var response = PagedMessageResponse()
        var page = PagedMessageResponsePage()
        page.page = 0
        page.pageSize = 10
        page.numberOfElements = 10
        page.totalElements = 100
        page.totalPages = 1
        response.page = page
        var items = MutableList<Message>(10) {
            messageDouble()}
        response.content = items
        return response
    }

    private fun messageDouble(): Message {
        var message = Message()
        message.id = UUID.randomUUID()
        message.status = Message.StatusEnum.WAITING
        message.toNumber = "+1234567890"
        message.fromNumber = "+10987654321"
        message.text = "Foo"
        message.createdAt = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
        message.updatedAt = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
        return message
    }

}
