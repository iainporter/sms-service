package com.porterhead.sms.provider

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.porterhead.sms.WiremockTestResource
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.clicksend.ClickSendData
import com.porterhead.sms.provider.clicksend.ClickSendProvider
import com.porterhead.sms.provider.clicksend.ClickSendRestClient
import com.porterhead.sms.provider.twilio.TwilioData
import com.porterhead.sms.provider.twilio.TwilioProvider
import com.porterhead.sms.resource.GetSmsMessageResourceTest
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.transaction.Transactional

@QuarkusTest
@QuarkusTestResource(WiremockTestResource::class)
@Transactional
class RandomProviderRouterTest {

    @Inject
    lateinit var providerRouter: RandomProviderRouter

    lateinit var mockServer : WireMockServer

    @Test
    fun `message is routed to random provider`() {
        setWiremocks_Success()
        val response = providerRouter.routeMessage(messageDouble())
        assert(response is ProviderResponse.SUCCESS)
    }

    @Test
    fun `message is routed to Twilio provider if ClickSend generates server exception`() {
        setTwilioWiremock(201, TwilioData().validResponse)
        setClickSendWiremock(200, ClickSendData().invalidRecipient)
        val response = providerRouter.routeMessage(messageDouble())
        assert(response is ProviderResponse.SUCCESS && response.providerName == TwilioProvider().getName())
    }

    @Test
    fun `message is routed to ClickSend provider if Twilio generates server exception`() {
        setTwilioWiremock(500, TwilioData().serviceUnavailable)
        setClickSendWiremock(200, ClickSendData().validResponse)
        val response = providerRouter.routeMessage(messageDouble())
        assert(response is ProviderResponse.SUCCESS && response.providerName == "ClickSend")

    }

    @Test
    fun `message fails when ClickSend and Twilio generate server exception`() {
        setTwilioWiremock(500, TwilioData().serviceUnavailable)
        setClickSendWiremock(200, ClickSendData().invalidRecipient)
        val response = providerRouter.routeMessage(messageDouble())
        assert(response is ProviderResponse.FAILED)
    }


    private fun setWiremocks_Success() {
        setClickSendWiremock(200, ClickSendData().validResponse)
        setTwilioWiremock(201, TwilioData().validResponse)
    }

    private fun setTwilioWiremock(status: Int, body: String) {
        mockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/twilio-wiremock"))
                .willReturn(WireMock.aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)))
    }

    private fun setClickSendWiremock(status: Int, body: String) {
        mockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/clicksend-wiremock"))
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
