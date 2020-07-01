package com.porterhead.sms.provider.twilio

import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.provider.SmsProvider
import com.twilio.exception.ApiException
import com.twilio.http.HttpMethod
import com.twilio.http.Request
import com.twilio.http.Response
import com.twilio.http.TwilioRestClient
import io.quarkus.arc.properties.IfBuildProperty
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@IfBuildProperty(name = "sms.provider.twilio.enabled", stringValue = "true")
class TwilioSmsService : SmsProvider {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java.simpleName)
    }

    @ConfigProperty(name = "twilio.account.sid")
    private val twilioSid: String? = null

    @ConfigProperty(name="twilio.endpoint")
    private val twilioEndpoint: String? = null

    @ConfigProperty(name ="twilio.auth.token")
    private val twilioAuthToken: String? = null

    @ConfigProperty(name="twilio.from.number")
    private val twilioFromNumber: String? = null

    override fun sendSms(message: SmsMessage) {
        log.debug("Sending SMS Message {} to number {}", message.text, message.toNumber)


        val twilioRestClient = TwilioRestClient.Builder(twilioSid, twilioAuthToken).build()
        val twilioRequest = buildTwilioRequest(message)

        val response: Response
        response = try {
            twilioRestClient.request(twilioRequest)
        } catch (e: ApiException) {
            throw RuntimeException(e)
        }

        log.debug("API response from Twilio, statusCode: {}", response.statusCode)
        if (response.statusCode != TwilioRestClient.HTTP_STATUS_CODE_CREATED) {
            log.debug("Response from Twilio was not 2xx: {}", response)
            throw RuntimeException("Response from Twilio was not 2xx")
        }
    }

    private fun buildTwilioRequest(message: SmsMessage): Request {
        val twilioRequest = Request(HttpMethod.POST, twilioEndpoint)
        val fromNumber = if (message.fromNumber.isNullOrEmpty())  twilioFromNumber else message.fromNumber
        twilioRequest.addPostParam("From", fromNumber)
        twilioRequest.addPostParam("To", message.toNumber)
        twilioRequest.addPostParam("Body", message.text)
        return twilioRequest
    }
}
