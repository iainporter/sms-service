package com.porterhead.sms.provider.clicksend

import ClickSend.Model.SmsMessageCollection
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.*

@RegisterRestClient
@RegisterProvider(ExceptionMapper::class)
@Path("/v3/sms/send")
interface ClickSendRestClient {

    @POST
    @Produces("application/json")
    @Consumes("application/json")
    fun sendSms(@HeaderParam("Authorization") authHeader: String, messages: SmsMessageCollection) : String

}
