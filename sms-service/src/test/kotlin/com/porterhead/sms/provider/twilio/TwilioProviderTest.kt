package com.porterhead.sms.provider.twilio

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.ProviderResponse
import com.porterhead.sms.resource.GetSmsMessageResourceTest
import com.twilio.http.TwilioRestClient
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TwilioProviderTest {

    val wireMockServer = WireMockServer(WireMockConfiguration().dynamicPort())
    var twilioProvider = TwilioProvider()

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        twilioProvider.accountSid = "AC" + RandomStringUtils.randomAlphanumeric(24)
        twilioProvider.authToken = RandomStringUtils.randomAlphanumeric(26)
        twilioProvider.fromNumber = "+1234567890"
        twilioProvider.endpoint = "http://localhost:${wireMockServer.port()}/twilio-mock"
    }

    @AfterAll
    fun tearDown() {
        wireMockServer.stop()
    }

    @BeforeEach
    fun reset() {
        wireMockServer.resetAll()
    }

    @Test
    fun `Send an Sms Message and expect 201`() {
        wireMockServer.stubFor(post(urlPathMatching("/twilio-mock"))
                .willReturn(aResponse()
                        .withStatus(TwilioRestClient.HTTP_STATUS_CODE_CREATED)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().validResponse)))
        val status = twilioProvider.sendSms(messageDouble())
        Assertions.assertTrue(status == ProviderResponse.SUCCESS(twilioProvider.getName()))
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/twilio-mock")))
    }

    @Test
    fun `Send an Sms Message and expect 401`() {
        wireMockServer.stubFor(post(urlPathMatching("/twilio-mock"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().unauthorizedResponse)))
        val status = twilioProvider.sendSms(messageDouble())
        Assertions.assertTrue(status is ProviderResponse.FAILED && status.failureMessage == "Credentials are invalid")    }

    @Test
    fun `Send an Sms Message and expect 400`() {
        wireMockServer.stubFor(post(urlPathMatching("/twilio-mock"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().badData)))
        val status = twilioProvider.sendSms(messageDouble())
        Assertions.assertTrue(status is ProviderResponse.FAILED && status.failureMessage == "There was an error with the request")
    }

    @Test
    fun `Send an Sms Message and expect 5xx`() {
        wireMockServer.stubFor(post(urlPathMatching("/twilio-mock"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().serviceUnavailable)))
        val status = twilioProvider.sendSms(messageDouble())
        Assertions.assertTrue(status is ProviderResponse.FAILED && status.failureMessage == "Non 201 response returned 500")
    }

    private fun messageDouble(): SmsMessage {
        return SmsMessage(id = GetSmsMessageResourceTest.id,
                fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }
}
