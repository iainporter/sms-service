package com.porterhead.sms.resource

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import com.porterhead.sms.SmsService
import com.porterhead.sms.domain.SmsMessage
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.h2.H2DatabaseTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.ws.rs.core.Response

@QuarkusTest
@QuarkusTestResource(value = H2DatabaseTestResource::class)
class SendSmsResourceTest {

    @InjectMock
    lateinit var smsService: SmsService

    @Test
    @DisplayName("POST /v1/sms with valid request returns 202")
    fun testSuccessfulSend() {
        whenever(smsService.createMessage(any())).thenReturn(testDouble())
        given()
                .`when`()
                .contentType(ContentType.JSON)
                .body("""{"text":"Hello World", "fromNumber":"+1234567890", "toNumber":"+1234567899"}""")
                .post("/v1/sms")
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.statusCode)
                .header("Location", Matchers.matchesPattern("http://localhost:8081/v1/sms/.+"))
    }

    @Test
    @DisplayName("Missing Required property returns a 400")
    fun testMissingTextProperty() {
        given()
                .`when`()
                .contentType(ContentType.JSON)
                .body("""{"fromNumber":"+1234567890", "toNumber":"+1234567899"}""")
                .post("/v1/sms")
                .then()
                .log().all()
                .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @DisplayName("Invalid phone number format returns a 400")
    fun testInvalidProperty() {
        given()
                .`when`()
                .contentType(ContentType.JSON)
                .body("""{"text":"Hello World", "fromNumber":"1234567890", "toNumber":"+1234567899"}""")
                .post("/v1/sms")
                .then()
                .log().all()
                .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    private fun testDouble(): SmsMessage {
        return SmsMessage(fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }
}
