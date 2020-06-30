package com.porterhead.sms

import com.porterhead.api.sms.PagedMessageResponse
import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.resource.PageableQuery
import java.util.*


interface SmsService {

    fun createMessage(request: SendSmsRequest): SmsMessage

    fun getMessage(id: UUID): SmsMessage

    fun getMessages(pageableQuery: PageableQuery): PagedMessageResponse
}
