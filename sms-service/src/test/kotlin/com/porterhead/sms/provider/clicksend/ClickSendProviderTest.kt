package com.porterhead.sms.provider.clicksend

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.ProviderResponse
import com.porterhead.sms.provider.twilio.TwilioData
import com.porterhead.sms.resource.GetSmsMessageResourceTest
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClickSendProviderTest {

    val wireMockServer = WireMockServer(WireMockConfiguration().dynamicPort())
    var provider = ClickSendProvider()

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        provider.apiKey = RandomStringUtils.randomAlphanumeric(24)
        provider.username = RandomStringUtils.randomAlphanumeric(10)
        provider.endpoint = "http://localhost:${wireMockServer.port()}/clicksend-mock"

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
    fun `Send an Sms Message and expect 200 and status s SUCCESS`() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-mock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ClickSendData().validResponse)))
        val status = provider.sendSms(messageDouble())
        assertTrue(status == ProviderResponse.SUCCESS(provider.getName()))
        wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/clicksend-mock")))
    }

    @Test
    fun `Send an Sms Message and expect 200 but status is INVALID_RECIPIENT`() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-mock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ClickSendData().invalidRecipient)))
        val status = provider.sendSms(messageDouble())
        assertTrue(status is ProviderResponse.FAILED && status.failureMessage == "INVALID_RECIPIENT")
    }

    @Test
    fun `Send an Sms Message and expect 401`() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-mock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().unauthorizedResponse)))
        val status = provider.sendSms(messageDouble())
        assertTrue(status is ProviderResponse.FAILED && status.failureMessage == "Unauthorized")
    }

    @Test
    fun `Send an Sms Message and get socket timeout`() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-mock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ClickSendData().validResponse)
                        .withFixedDelay(6000))) //1 sec longer than the configured read timeout
        val status = provider.sendSms(messageDouble())
        assertTrue(status is ProviderResponse.FAILED)
    }

    private fun messageDouble(): SmsMessage {
        return SmsMessage(id = GetSmsMessageResourceTest.id,
                fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }

}
