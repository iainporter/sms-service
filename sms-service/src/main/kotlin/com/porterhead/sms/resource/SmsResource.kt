package com.porterhead.sms.resource

import com.porterhead.sms.toMessageResponse
import com.porterhead.api.sms.Message
import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.SmsService
import com.porterhead.sms.domain.MessageStatus
import com.porterhead.sms.domain.SmsMessage
import io.quarkus.panache.common.Page
import io.quarkus.security.Authenticated
import java.util.*
import javax.enterprise.context.RequestScoped
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
@RequestScoped
class SmsResource {

    @Inject
    @field: Default
    lateinit var smsService: SmsService

    @POST
    @Authenticated
    fun sendSms(@Valid smsRequest: SendSmsRequest, @Context uriInfo: UriInfo): Response {
        val message: SmsMessage = smsService.createMessage(smsRequest)
        // build the Location header
        val builder = uriInfo.absolutePathBuilder
        val uriComponents = builder.path("/{id}").build(message.id)

        // return a 202 Accepted response with the correct Location header
        return Response.accepted().location(uriComponents.normalize()).build()
    }

    @GET
    @Authenticated
    fun queryForMessages(@QueryParam("status") status: Message.StatusEnum?,
                         @QueryParam("toNumber") toNumber: String?,
                         @QueryParam("page") page: Int?,
                         @QueryParam("pageSize") pageSize: Int?,
                         @QueryParam("sort") @DefaultValue("updatedAt:desc") sort: String): Response {
        val page: Page = Page.of(page ?: 0, pageSize ?: 25)
        val status = (if (status != null) MessageStatus.valueOf(status.name) else null)
        val results = smsService.getMessages(PageableQuery(page, QueryRequest
                .Builder()
                .status(status)
                .toNumber(toNumber)
                .build()))
        return Response.ok(results).build()
    }

    @GET
    @Path("/{id}")
    @Authenticated
    fun getMessage(@PathParam("id") id: UUID): Response {
        val message: SmsMessage = smsService.getMessage(id)
        return Response.ok(message.toMessageResponse()).build()
    }

}
