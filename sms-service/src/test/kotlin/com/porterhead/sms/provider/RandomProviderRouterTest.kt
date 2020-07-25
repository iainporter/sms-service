package com.porterhead.sms.provider

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.porterhead.sms.domain.MessageStatus.DELIVERED
import com.porterhead.sms.domain.MessageStatus.FAILED
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.clicksend.ClickSendData
import com.porterhead.sms.provider.clicksend.ClickSendProvider
import com.porterhead.sms.provider.twilio.TwilioData
import com.porterhead.sms.provider.twilio.TwilioProvider
import com.porterhead.sms.resource.GetSmsMessageResourceTest
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.*
import javax.inject.Inject
import javax.transaction.Transactional

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
@Transactional
class RandomProviderRouterTest {

    @Inject
    lateinit var providerRouter: RandomProviderRouter

    //TODO figure out how to set dynamic port in application-test.properties
    val wireMockServer = WireMockServer(WireMockConfiguration().port(2345))

    @BeforeAll
    fun setup() {
        wireMockServer.start()
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
    fun `message is routed to random provider`() {
        setWiremocks_Success()
        val message = providerRouter.routeMessage(messageDouble())
        assert(message.status == DELIVERED)
    }

    @Test
    fun `message is routed to Twilio provider if ClickSend generates server exception`() {
        setTwilioWiremock(201, TwilioData().validResponse)
        setClickSendWiremock(200, ClickSendData().invalidRecipient)
        val message = providerRouter.routeMessage(messageDouble())
        assert(message.status == DELIVERED)
        assert(message.provider == TwilioProvider().getName())
    }

    @Test
    fun `message is routed to ClickSend provider if Twilio generates server exception`() {
        setTwilioWiremock(500, TwilioData().serviceUnavailable)
        setClickSendWiremock(200, ClickSendData().validResponse)
        val message = providerRouter.routeMessage(messageDouble())
        assert(message.status == DELIVERED)
        assert(message.provider == ClickSendProvider().getName())
    }

    @Test
    fun `message fails when ClickSend and Twilio generate server exception`() {
        setTwilioWiremock(500, TwilioData().serviceUnavailable)
        setClickSendWiremock(200, ClickSendData().invalidRecipient)
        val message = providerRouter.routeMessage(messageDouble())
        assert(message.status == FAILED)
    }


    private fun setWiremocks_Success() {
        setClickSendWiremock(200, ClickSendData().validResponse)
        setTwilioWiremock(201, TwilioData().validResponse)
    }

    private fun setTwilioWiremock(status: Int, body: String) {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/twilio-wiremock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)))
    }

    private fun setClickSendWiremock(status: Int, body: String) {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-wiremock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)))
    }

    private fun messageDouble(): SmsMessage {
        return SmsMessage(id = GetSmsMessageResourceTest.id,
                fromNumber = "+1234567890",
                toNumber = "+1234567899",
                text = "Hello World")
    }
}
