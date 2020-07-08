package com.porterhead.openapi.sms

import com.porterhead.api.sms.SendSmsRequest
import org.junit.Test
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import javax.validation.ConstraintViolation
import javax.validation.Validation
import kotlin.test.assertEquals

class SendSmsRequestTest {

    val mapper = ObjectMapper().registerModule(KotlinModule())

    @Test
    fun `all properties are valid`() {
        val json = """{"text":"Hello World", "toNumber":"+44555225555", "fromNumber": "+44123123123"}"""
        val request = mapper.readValue<SendSmsRequest>(json)
        val validator = Validation.buildDefaultValidatorFactory().getValidator()
        val results = validator.validate(request)
        assertEquals(0, results.size)
    }

    @Test
    fun `missing text property`() {
        val json = """{"toNumber":"+44555225555", "fromNumber": "+44123123123"}"""
        val results = validate(json)
        assertEquals(1, results?.size)
        assertEquals("must not be null", results?.filter { it.propertyPath.toString().equals("text") }?.first()!!.message)
    }

    @Test
    fun `missing toNumber property`() {
        val json = """{"text":"Hello World", "fromNumber": "+44123123123"}"""
        val results = validate(json)
        assertEquals(1, results?.size)
        assertEquals("must not be null", results?.filter { it.propertyPath.toString().equals("toNumber") }?.first()!!.message)
    }

    @Test
    fun `Invalid Phone Number`() {
        val json = """{"text":"Hello World", "toNumber":"44555225555", "fromNumber": "+44123123123"}"""
        val request = mapper.readValue<SendSmsRequest>(json)
        val validator = Validation.buildDefaultValidatorFactory().getValidator()
        val results = validator.validate(request)
        assertEquals(1, results?.size)
        assertEquals("must match \"^\\+[1-9]\\d{1,14}\$\"", results?.filter { it.propertyPath.toString().equals("toNumber") }?.first()!!.message)
    }

    private fun validate(json: String): Set<ConstraintViolation<SendSmsRequest>>? {
        val request = mapper.readValue<SendSmsRequest>(json)
        val validator = Validation.buildDefaultValidatorFactory().getValidator()
        val results = validator.validate(request)
        return results
    }

}
