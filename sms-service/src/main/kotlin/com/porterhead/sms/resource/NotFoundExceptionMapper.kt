package com.porterhead.sms.resource

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * TODO: provide a generic exception handling library that returns error messages in a format defined by the API
 */
@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException>{

    override fun toResponse(ex: NotFoundException?): Response {
        val message = "Resource not found"
        return Response.status(404).entity(message).build()
    }
}
