package com.porterhead.sms.provider.clicksend

import ClickSend.Api.SmsApi
import ClickSend.ApiClient
import ClickSend.ApiException
import ClickSend.Model.SmsMessageCollection
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.SmsProvider
import io.quarkus.arc.properties.IfBuildProperty
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped


@IfBuildProperty(name = "sms.provider.clicksend.enabled", stringValue = "true")
@ApplicationScoped
class ClickSendProvider: SmsProvider {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java.simpleName)
    }

    @ConfigProperty(name = "clicksend.username")
    private val username: String? = null

    @ConfigProperty(name="clicksend.apiKey")
    private val apiKey: String? = null

    override fun sendSms(message: SmsMessage) {
        log.debug("Sending SMS via Click Send Service to {}", message.toNumber)
        val defaultClient = ApiClient()
        defaultClient.setUsername(username)
        defaultClient.setPassword(apiKey)
        val apiInstance = SmsApi(defaultClient)

        val smsMessage = ClickSend.Model.SmsMessage()
        smsMessage.body(message.text)
        smsMessage.to(message.toNumber)
        smsMessage.source(message.fromNumber)

        val smsMessageList: List<ClickSend.Model.SmsMessage> = mutableListOf(smsMessage)
        val smsMessages = SmsMessageCollection()
        smsMessages.messages(smsMessageList)
        try {
            val result = apiInstance.smsSendPost(smsMessages)
            println(result)
        } catch (e: ApiException) {
            System.err.println("Exception when calling SmsApi#smsSendPost")
            e.printStackTrace()
        }
    }
}
