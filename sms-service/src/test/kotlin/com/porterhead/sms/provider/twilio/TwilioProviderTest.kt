package com.porterhead.sms.provider.twilio

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.BadRequestException
import com.porterhead.sms.provider.ServerException
import com.porterhead.sms.provider.UnauthorizedException
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
        twilioProvider.sendSms(messageDouble())
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/twilio-mock")))
    }

    @Test
    fun `Send an Sms Message and expect 401`() {
        wireMockServer.stubFor(post(urlPathMatching("/twilio-mock"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().unauthorizedResponse)))
        Assertions.assertThrows(UnauthorizedException::class.java) { twilioProvider.sendSms(messageDouble()) }
    }

    @Test
    fun `Send an Sms Message and expect 400`() {
        wireMockServer.stubFor(post(urlPathMatching("/twilio-mock"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().badData)))
        Assertions.assertThrows(BadRequestException::class.java) { twilioProvider.sendSms(messageDouble()) }
    }

    @Test
    fun `Send an Sms Message and expect 5xx`() {
        wireMockServer.stubFor(post(urlPathMatching("/twilio-mock"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().serviceUnavailable)))
        Assertions.assertThrows(ServerException::class.java) { twilioProvider.sendSms(messageDouble()) }
    }

    private fun messageDouble(): SmsMessage {
        return SmsMessage(id = GetSmsMessageResourceTest.id,
                fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }
}
