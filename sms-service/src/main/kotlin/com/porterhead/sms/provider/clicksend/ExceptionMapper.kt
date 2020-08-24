package com.porterhead.sms.provider.clicksend

import mu.KotlinLogging
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper
import java.io.ByteArrayInputStream
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response


class ExceptionMapper: ResponseExceptionMapper<RuntimeException> {

    private val log = KotlinLogging.logger {}

    override fun toThrowable(response: Response): RuntimeException {
        log.debug { "ClickSend returned a non-200 response ${response.status}" }
        return WebApplicationException(getBody(response))
    }

    private fun getBody(response: Response): String {
        val inputStream = response.entity as ByteArrayInputStream
        val bytes = ByteArray(inputStream.available())
        inputStream.read(bytes, 0, inputStream.available())
        return String(bytes)
    }
}
