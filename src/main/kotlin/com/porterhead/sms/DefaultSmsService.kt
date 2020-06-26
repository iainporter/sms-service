package com.porterhead.sms

import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.MessageStatus
import com.porterhead.sms.domain.SmsMessage
import java.time.Instant
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.transaction.Transactional

@ApplicationScoped
class DefaultSmsService: SmsService {

    @Inject
    lateinit var entityManager: EntityManager

    @Transactional
    override fun createMessage(request: SendSmsRequest): SmsMessage {
        var entity = SmsMessage(
                fromNumber = request.fromNumber,
                toNumber = request.toNumber,
                text = request.text)
        entityManager.persist(entity)
        return entity
    }

}
