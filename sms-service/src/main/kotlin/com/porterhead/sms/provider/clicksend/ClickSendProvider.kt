package com.porterhead.sms.provider.clicksend

import ClickSend.ApiClient
import ClickSend.ApiException
import ClickSend.Model.SmsMessageCollection
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.BadRequestException
import com.porterhead.sms.provider.ServerException
import com.porterhead.sms.provider.SmsProvider
import com.porterhead.sms.provider.UnauthorizedException
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import io.quarkus.arc.properties.IfBuildProperty
import mu.KotlinLogging
import okhttp3.Response
import okhttp3.ResponseBody
import org.eclipse.microprofile.config.inject.ConfigProperty
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

    @PostConstruct
    fun init() {
        apiCreds = Base64.getEncoder().encodeToString("$username:$apiKey".toByteArray())
    }

    val gson = GsonBuilder().create()

    override fun sendSms(message: SmsMessage) {
        log.debug("Sending SMS via Click Send Service to {}", message.toNumber)
        val defaultClient = ApiClient()
        defaultClient.setUsername(username)
        defaultClient.setPassword(apiKey)
        val smsMessage = ClickSend.Model.SmsMessage()
        smsMessage.body(message.text)
        smsMessage.to(message.toNumber)
        smsMessage.source(message.fromNumber)

        val smsMessageList: List<ClickSend.Model.SmsMessage> = mutableListOf(smsMessage)
        val smsMessages = SmsMessageCollection()
        smsMessages.messages(smsMessageList)
        val response =
                try {
                    postToApi(smsMessages)
                } catch (e: ApiException) {
                    log.debug { "Post to ClickSend API failed $e" }
                    throw ServerException(e.message!!)
                }
        log.debug("API response from ClickSend, statusCode: {}", response.code())
        when (response.code()) {
            200 -> {
                //failed requests will be successful with a status code in the message indicating the failure
                response.body()?.let { extractStatus(it) }
            }
            400 -> {
                log.debug { "got 400 response back from ClickSend ${gson.fromJson(response.body()?.string(), JsonObject::class.java)}" }
                throw BadRequestException("There was an error with the request")
            }
            401 -> throw UnauthorizedException("ClickSend credentials are invalid")
            else -> {
                log.debug { "got non-200 response back from ClickSend ${response.body().toString()}" }
                throw ServerException("Non 2xx response returned from ClickSend")
            }
        }
    }


    override fun getName(): String {
        return "ClickSend"
    }

    /**
     * Extract the status from the response body
     * Assume only one message was sent
     * The path in the response body is data.messages[0].status
     */
    private fun extractStatus(body: ResponseBody) {
        val jsonObject = gson.fromJson(body.string(), JsonObject::class.java)
        val status = ((jsonObject.get("data") as JsonObject).getAsJsonArray("messages")[0] as JsonObject).getAsJsonPrimitive("status").asString
        if (status != "SUCCESS") {
            throw ServerException("Message was rejected with status $status")
        }
    }

    private fun postToApi(smsMessages: SmsMessageCollection): Response {
        val bodyAsJson = gson.toJson(smsMessages)
        log.debug { "serialised messages to json: $bodyAsJson" }
        return httpPost {
            url(URL(endpoint))
            header { "Authorization" to "Basic $apiCreds" }
            header { "Content-Type" to "application/json" }
            body("application/json") {
                json(bodyAsJson)
            }
        }
    }
}
