package com.porterhead.testing

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response

class RestFunctions(private val authServerPort: Int) {

    private val token = getAccessToken()

    fun sendSmsMessage(request: String, expectedStatus: Int = 202): Response {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
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
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response()
    }

    fun getMessageById(id: String, expectedStatus: Int = 200): Response {
        return RestAssured.given()
                .auth().oauth2(token)
                .get("/v1/sms/$id")
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response()
    }

    fun getMessages(page: Int = 0, pageSize: Int = 10, expectedStatus: Int = 200): Response {
        return RestAssured.given()
                .auth().oauth2(token)
                .queryParam("page", page)
                .queryParam("pageSize", pageSize)
                .get("v1/sms")
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response()
    }

    /**
     * Get a JWT from the local Keycloak instance
     * Use the credentials for a client defined in config/porterhead-realm.json
     */
    private fun getAccessToken(): String {
        val clientId = "backend-service"
        val clientSecret = "8155b2ad-cd9d-48ae-a5e1-ea11d5cfcb79"
        val response = RestAssured.given()
                .auth().preemptive().basic(clientId, clientSecret)
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "client_credentials")
                .post("http://localhost:$authServerPort/auth/realms/porterhead/protocol/openid-connect/token")
                .then()
                .extract()
                .response()
        return response.body.jsonPath().getString("access_token")
    }
}
