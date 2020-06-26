package com.porterhead.sms

import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.jpa.MessageRepository
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.NotFoundException

@ApplicationScoped
class DefaultSmsService: SmsService {

    @Inject
    lateinit var messageRepository: MessageRepository

    @Transactional
    override fun createMessage(request: SendSmsRequest): SmsMessage {
        var entity = SmsMessage(
                fromNumber = request.fromNumber,
                toNumber = request.toNumber,
                text = request.text)
        messageRepository.persist(entity)
        return entity
    }

    override fun getMessage(id: UUID): SmsMessage {
        val message: Optional<SmsMessage> = messageRepository.findByIdOptional(id)
        if (!message.isPresent) {
            throw NotFoundException()
        }
        return message.get()
    }
}
