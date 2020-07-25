package com.porterhead.testing

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response

object RestFunctions {

    /** This number will be mapped to success for all providers*/
    val successToNumberAll = "+1111111111"

    /** This number will be mapped to success for clicksend but not Twilio*/
    val successToNumberClicksend = "+2222222222"

    /** This number will be mapped to success for twilio but not ClickSend*/
    val successToNumberTwilio = "+3333333333"

    /** This number will be mapped to failure for twilio and ClickSend*/
    val failureToNumberAll = "+4444444444"

    fun sendSmsMessage(request: String, expectedStatus: Int = 202): Response {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/v1/sms")
                .then()
                .assertThat()
                .statusCode(expectedStatus)
                .extract()
                .response()
    }

    fun getMessage(location: String, expectedStatus: Int = 200): Response {
        return RestAssured.given()
                .get(location)
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response()
    }

    fun getMessageById(id: String, expectedStatus: Int = 200): Response {
        return RestAssured.given()
                .get("/v1/sms/$id")
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response()
    }

    fun getMessages(page: Int = 0, pageSize: Int = 10, expectedStatus: Int = 200): Response {
        return RestAssured.given()
                .queryParam("page", page)
                .queryParam("pageSize", pageSize)
                .get("v1/sms")
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response()
    }
}
