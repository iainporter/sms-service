package com.porterhead.sms.jpa

import com.porterhead.sms.domain.SmsMessage
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MessageRepository : PanacheRepositoryBase<SmsMessage, UUID> {

}
