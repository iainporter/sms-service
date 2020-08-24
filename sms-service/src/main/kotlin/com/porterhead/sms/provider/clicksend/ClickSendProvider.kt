package com.porterhead.sms.provider.clicksend

import ClickSend.Model.SmsMessageCollection
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.ProviderResponse
import com.porterhead.sms.provider.SmsProvider
import mu.KotlinLogging
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.ProcessingException
import javax.ws.rs.WebApplicationException


@ApplicationScoped
class ClickSendProvider(@RestClient var clickSendRestClient: ClickSendRestClient) : SmsProvider {

    private val log = KotlinLogging.logger {}

    @ConfigProperty(name = "sms.provider.clicksend.username")
    var username: String? = null

    @ConfigProperty(name = "sms.provider.clicksend.apiKey")
    var apiKey: String? = null

    var apiCreds: String = Base64.getEncoder().encodeToString("$username:$apiKey".toByteArray())


    val gson: Gson = GsonBuilder().create()

    override fun sendSms(message: SmsMessage): ProviderResponse {
        log.debug("Sending SMS via ClickSend Service to {}", message.toNumber)
        val smsMessages = buildRequest(message)
        return try {
            val response = clickSendRestClient.sendSms("Basic $apiCreds", smsMessages)
            val status = extractStatus(response)
            if (status == "SUCCESS") {
                ProviderResponse.SUCCESS(getName())
            } else {
                log.debug { "ClickSend failed to send the message due to: $status" }
                ProviderResponse.FAILED(getName(), status)
            }
        } catch (e: WebApplicationException) {
            log.debug { "Post to ClickSend API failed $e" }
            ProviderResponse.FAILED(getName(), e.localizedMessage)
        } catch (e: ProcessingException) {
            log.debug { "Post to ClickSend API failed with timeout $e" }
            ProviderResponse.FAILED(getName(), "Connection timeout")
        }
    }


    override fun getName(): String {
        return "ClickSend"
    }

    private fun buildRequest(message: SmsMessage): SmsMessageCollection {
        val smsMessage = ClickSend.Model.SmsMessage()
        smsMessage.body(message.text)
        smsMessage.to(message.toNumber)
        smsMessage.source(message.fromNumber)

        val smsMessageList: List<ClickSend.Model.SmsMessage> = mutableListOf(smsMessage)
        val smsMessages = SmsMessageCollection()
        smsMessages.messages(smsMessageList)
        return smsMessages
    }

    /**
     * Extract the status from the response body
     * Assume only one message was sent
     * The path in the response body is data.messages[0].status
     */
    private fun extractStatus(response: String): String {
        val jsonObject = gson.fromJson(response, JsonObject::class.java)
        return ((jsonObject.get("data") as JsonObject)
                .getAsJsonArray("messages")[0] as JsonObject)
                .getAsJsonPrimitive("status").asString
    }

//    private fun postToApi(smsMessages: SmsMessageCollection): Response {
//        val bodyAsJson = gson.toJson(smsMessages)
//        log.debug { "serialised messages to json: $bodyAsJson" }
//        return httpPost(client) {
//            url(URL(endpoint))
//            header { "Authorization" to "Basic $apiCreds" }
//            header { "Content-Type" to "application/json" }
//            body("application/json") {
//                json(bodyAsJson)
//            }
//        }
//    }

    override fun toString(): String {
        return "ClickSendProvider(username=$username)"
    }

}
