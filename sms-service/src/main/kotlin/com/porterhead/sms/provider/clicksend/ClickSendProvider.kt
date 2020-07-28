package com.porterhead.sms.provider.clicksend

import ClickSend.Model.SmsMessageCollection
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.*
import io.github.rybalkinsd.kohttp.client.defaultHttpClient
import io.github.rybalkinsd.kohttp.client.fork
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import io.quarkus.arc.properties.IfBuildProperty
import mu.KotlinLogging
import okhttp3.Response
import okhttp3.ResponseBody
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped


@IfBuildProperty(name = "sms.provider.clicksend.enabled", stringValue = "true")
@ApplicationScoped
class ClickSendProvider : SmsProvider {

    private val log = KotlinLogging.logger {}

    @ConfigProperty(name = "sms.provider.clicksend.username")
    var username: String? = null

    @ConfigProperty(name = "sms.provider.clicksend.apiKey")
    var apiKey: String? = null

    @ConfigProperty(name = "sms.provider.clicksend.endpoint")
    var endpoint: String? = null

    var apiCreds: String? = null

    val client = defaultHttpClient.fork {
        connectTimeout = 5000
        writeTimeout = 5000
        readTimeout = 5000
    }

    @PostConstruct
    fun init() {
        apiCreds = Base64.getEncoder().encodeToString("$username:$apiKey".toByteArray())
    }

    val gson = GsonBuilder().create()

    override fun sendSms(message: SmsMessage) : ProviderResponse {
        log.debug("Sending SMS via ClickSend Service to {}", message.toNumber)
        val smsMessages = buildRequest(message)
        val response =
                try {
                    postToApi(smsMessages)
                } catch (e: SocketTimeoutException) {
                    log.debug { "Post to ClickSend API failed $e" }
                    return ProviderResponse.FAILED(getName(), e.localizedMessage)
                }
        log.debug("API response from ClickSend, statusCode: {}", response.code())
        when (response.code()) {
            200 -> {
                val status: String = response.body()?.let { extractStatus(it) }?: "Unknown Status"
                //failed requests will be successful with a status code in the message indicating the failure
                return if (status == "SUCCESS") {
                    ProviderResponse.SUCCESS(getName())
                } else {
                    ProviderResponse.FAILED(getName(), status)
                }
            }
            400 -> {
                log.debug { "got 400 response back from ClickSend ${gson.fromJson(response.body()?.string(), JsonObject::class.java)}" }
                return ProviderResponse.FAILED(getName(),"The request is invalid")
            }
            401 -> return ProviderResponse.FAILED(getName(),"Unauthorized")
            else -> {
                log.debug { "got non-200 response back from ClickSend ${response.code()}" }
                return ProviderResponse.FAILED(getName(),"Response status : ${response.code()}")
            }
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
    private fun extractStatus(body: ResponseBody): String {
        val jsonObject = gson.fromJson(body.string(), JsonObject::class.java)
        return ((jsonObject.get("data") as JsonObject)
                .getAsJsonArray("messages")[0] as JsonObject)
                .getAsJsonPrimitive("status").asString
    }

    private fun postToApi(smsMessages: SmsMessageCollection): Response {
        val bodyAsJson = gson.toJson(smsMessages)
        log.debug { "serialised messages to json: $bodyAsJson" }
        return httpPost(client) {
            url(URL(endpoint))
            header { "Authorization" to "Basic $apiCreds" }
            header { "Content-Type" to "application/json" }
            body("application/json") {
                json(bodyAsJson)
            }
        }
    }

    override fun toString(): String {
        return "ClickSendProvider(username=$username, endpoint=$endpoint)"
    }

}
