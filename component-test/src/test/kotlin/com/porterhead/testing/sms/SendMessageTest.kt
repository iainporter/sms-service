package com.porterhead.testing.sms

import com.porterhead.testing.util.TestEnvironment
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class SendMessageTest : TestEnvironment() {

    @Test
    fun `a valid request to send a SMS Message`() {
        val response : Response  = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"+1234567899"}""")
                .post("/v1/sms")
                .then()
                .assertThat()
                .statusCode(202)
                .extract()
                .response()
        val location = response.header("Location")
        //check the message can be retrieved
        RestAssured.given()
                .get(location)
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", `is`(location.substring(location.lastIndexOf("/")+ 1)))
                .body("status", `is`("WAITING"))
                .body("toNumber", `is`("+1234567899"))
                .body("fromNumber", `is`("+1234567890"))
                .body("text", `is`("Foo Bar"))

    }
}
