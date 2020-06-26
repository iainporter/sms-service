package com.porterhead.sms.resource

import com.porterhead.api.sms.Message
import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.SmsService
import com.porterhead.sms.domain.SmsMessage
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo


@Path("/v1/sms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SmsResource {

    @Inject
    @field: Default
    lateinit var smsService: SmsService

    @POST
    fun sendSms(@Valid smsRequest: SendSmsRequest, @Context uriInfo: UriInfo): Response {
        val message: SmsMessage = smsService.createMessage(smsRequest)
        // build the Location header
        val builder = uriInfo.absolutePathBuilder
        val uriComponents = builder.path("/{id}").build(message.id)

        // return a 202 Accepted response with the correct Location header
        return Response.accepted().location(uriComponents.normalize()).build()
    }

    @GET
    @Path("/{id}")
    fun getMessage(@PathParam("id") id: UUID): Response {
        val message: SmsMessage = smsService.getMessage(id)
        return Response.ok(message.toMessageResponse()).build()
    }

    private fun SmsMessage.toMessageResponse(): Message  {
        val message: Message = Message()
        message.id = id
        message.fromNumber= fromNumber
        message.toNumber = toNumber
        message.text = text
        message.status = Message.StatusEnum.fromValue(status.name)
        message.createdAt = OffsetDateTime.ofInstant(createdAt, ZoneId.of("UTC"))
        message.updatedAt = OffsetDateTime.ofInstant(updatedAt, ZoneId.of("UTC"))
        return message
    }
}
