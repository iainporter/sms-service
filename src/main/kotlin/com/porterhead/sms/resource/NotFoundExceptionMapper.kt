package com.porterhead.sms.resource

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException>{

    override fun toResponse(ex: NotFoundException?): Response {
        val message: String = "Resource not found"
        return Response.status(404).entity(message).build()
    }
}
