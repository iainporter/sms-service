package com.porterhead.sms.resource

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import com.porterhead.sms.SmsService
import com.porterhead.sms.WiremockTestResource
import com.porterhead.sms.domain.SmsMessage
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.ws.rs.core.Response

@QuarkusTest
class SendSmsResourceTest : WiremockTestResource(){

    @InjectMock
    lateinit var smsService: SmsService

    @Test
    @DisplayName("POST /v1/sms with valid request returns 202")
    fun testSuccessfulSend() {
        whenever(smsService.createMessage(any())).thenReturn(testDouble())
        given()
                .`when`()
                .contentType(ContentType.JSON)
                .auth().oauth2(generateJWT(keyPair))
                .body("""{"text":"Hello World", "fromNumber":"+1234567890", "toNumber":"+1234567899"}""")
                .post("/v1/sms")
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.statusCode)
                .header("Location", Matchers.matchesPattern("http://localhost:8081/v1/sms/.+"))
    }

    @Test
    @DisplayName("POST /v1/sms fails with 401 due to no bearer token")
    fun testUnauthorized() {
        given()
                .`when`()
                .contentType(ContentType.JSON)
                .body("""{"text":"Hello World", "fromNumber":"+1234567890", "toNumber":"+1234567899"}""")
                .post("/v1/sms")
                .then()
                .log().all()
                .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    @DisplayName("POST /v1/sms fails with 403 due to invalid signature")
    fun testForbidden() {
        //create a new signing key that is  unknown to the OIDC server
        val unknownKeyPair = generatePrivateKey()
        given()
                .`when`()
                .contentType(ContentType.JSON)
                .auth().oauth2(generateJWT(unknownKeyPair))
                .body("""{"text":"Hello World", "fromNumber":"+1234567890", "toNumber":"+1234567899"}""")
                .post("/v1/sms")
                .then()
                .log().all()
                .statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    @DisplayName("Missing Required property returns a 400")
    fun testMissingTextProperty() {
        given()
                .`when`()
                .contentType(ContentType.JSON)
                .auth().oauth2(generateJWT(keyPair))
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
                .auth().oauth2(generateJWT(keyPair))
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
