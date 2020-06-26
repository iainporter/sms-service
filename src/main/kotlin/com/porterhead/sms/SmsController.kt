package com.porterhead.sms

import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.SmsMessage
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo


@Path("/v1/sms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SmsController {

    @Inject
    @field: Default
    lateinit var smsService: SmsService

    @POST
    fun sendSms(smsRequest: SendSmsRequest, @Context uriInfo: UriInfo): Response {
        val message: SmsMessage = smsService.createMessage(smsRequest)
        // build the Location header
        val builder = uriInfo.absolutePathBuilder
        val uriComponents = builder.path("/{id}").build(message.id)

        // return a 202 Accepted response with the correct Location header
        return Response.accepted().location(uriComponents.normalize()).build()
    }
}
