package com.porterhead.sms.resource

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import com.porterhead.sms.SmsService
import com.porterhead.sms.domain.SmsMessage
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.h2.H2DatabaseTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response

@QuarkusTest
@QuarkusTestResource(value = H2DatabaseTestResource::class)
class GetSmsMessageResourceTest {

    companion object {
        val id: UUID = UUID.fromString("47b56dd0-73d4-485e-b5e7-0489865973a1")
    }

    @InjectMock
    lateinit var smsService: SmsService

    @Test
    @DisplayName("GET /v1/sms/{id} returns 200")
    fun testGetMessage() {
        whenever(smsService.getMessage(any())).thenReturn(testDouble())
        RestAssured.given()
                .`when`()
                .contentType(ContentType.JSON)
                .get("/v1/sms/$id")
                .then()
                .log().all()
                .statusCode(Response.Status.OK.statusCode)
    }

    @Test
    @DisplayName("GET /v1/sms/{id} returns 404")
    fun testGetMessageNotFound() {
        whenever(smsService.getMessage(any())).thenThrow(NotFoundException::class.java)
        RestAssured.given()
                .`when`()
                .contentType(ContentType.JSON)
                .get("/v1/sms/$id")
                .then()
                .log().all()
                .statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    private fun testDouble(): SmsMessage {
        return SmsMessage(id = id,
                fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }
}
