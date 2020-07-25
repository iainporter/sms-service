package com.porterhead.sms.provider.twilio

import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.BadRequestException
import com.porterhead.sms.provider.ServerException
import com.porterhead.sms.provider.SmsProvider
import com.porterhead.sms.provider.UnauthorizedException
import com.twilio.exception.ApiException
import com.twilio.http.HttpMethod
import com.twilio.http.Request
import com.twilio.http.Response
import com.twilio.http.TwilioRestClient
import io.quarkus.arc.properties.IfBuildProperty
import mu.KotlinLogging
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@IfBuildProperty(name = "sms.provider.twilio.enabled", stringValue = "true")
class TwilioProvider : SmsProvider {

    private val log = KotlinLogging.logger {}

    @ConfigProperty(name = "sms.provider.twilio.account.sid")
    var accountSid: String? = null

    @ConfigProperty(name = "sms.provider.twilio.endpoint")
    var endpoint: String? = null

    @ConfigProperty(name = "sms.provider.twilio.auth.token")
    var authToken: String? = null

    @ConfigProperty(name = "sms.provider.twilio.from.number")
    var fromNumber: String? = null

    override fun sendSms(message: SmsMessage) {
        log.debug("Sending SMS via Twilio Service to {}", message.toNumber)
        val twilioRestClient = TwilioRestClient.Builder(accountSid, authToken).build()
        val twilioRequest = buildTwilioRequest(message)

        val response: Response
        response = try {
            twilioRestClient.request(twilioRequest)
        } catch (e: ApiException) {
            log.debug("Send Message failed", e)
            throw ServerException(e.message!!)
        }

        log.debug("API response from Twilio, statusCode: {}", response.statusCode)
        when (response.statusCode) {
            201 -> return
            400 -> throw BadRequestException("There was an error with the request")
            401 -> throw UnauthorizedException("Twilio credentials are invalid")
            else -> {
                log.debug { "Non 201 response returned from Twilio: ${response.statusCode}" }
                throw ServerException("Non 201 response returned from Twilio")}
        }
    }

    override fun getName(): String {
        return "Twilio"
    }

    private fun buildTwilioRequest(message: SmsMessage): Request {
        val twilioRequest = Request(HttpMethod.POST, endpoint)
        val fromNumber = if (message.fromNumber.isNullOrEmpty()) fromNumber else message.fromNumber
        twilioRequest.addPostParam("From", fromNumber)
        twilioRequest.addPostParam("To", message.toNumber)
        twilioRequest.addPostParam("Body", message.text)
        return twilioRequest
    }

    override fun toString(): String {
        return "TwilioProvider(accountSid=$accountSid, endpoint=$endpoint)"
    }


}
