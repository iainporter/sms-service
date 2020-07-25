package com.porterhead.sms.provider.clicksend

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.ServerException
import com.porterhead.sms.provider.UnauthorizedException
import com.porterhead.sms.provider.twilio.TwilioData
import com.porterhead.sms.resource.GetSmsMessageResourceTest
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.*

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
        provider.sendSms(messageDouble())
        wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/clicksend-mock")))
    }

    @Test
    fun `Send an Sms Message and expect 200 but status is INVALID_RECIPIENT`() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-mock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ClickSendData().invalidRecipient)))
        Assertions.assertThrows(ServerException::class.java) { provider.sendSms(messageDouble()) }
    }

    @Test
    fun `Send an Sms Message and expect 401`() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-mock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TwilioData().unauthorizedResponse)))
        Assertions.assertThrows(UnauthorizedException::class.java) { provider.sendSms(messageDouble()) }
    }

    private fun messageDouble(): SmsMessage {
        return SmsMessage(id = GetSmsMessageResourceTest.id,
                fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }

}
