package com.porterhead.sms.provider

import com.porterhead.sms.domain.MessageStatus
import com.porterhead.sms.domain.SmsMessage
import mu.KotlinLogging
import java.util.*
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance
import javax.inject.Inject
import kotlin.system.exitProcess

@ApplicationScoped
class RandomProviderRouter : ProviderRouter {

    private val log = KotlinLogging.logger {}

    @Inject
    lateinit var providers: Instance<SmsProvider>

    var random: Random = Random()

    override fun routeMessage(message: SmsMessage): SmsMessage {
        val status = routeMessageInternal(message)
        if (status is ProviderResponse.SUCCESS) {
            message.status = MessageStatus.DELIVERED
        } else {
            message.status = MessageStatus.FAILED
        }
        return message
    }

    /**
     * Route the message to a random Provider from the list of Providers
     * If there is a server failure then pick another unused provider to try the message again
     */
    private fun routeMessageInternal(message: SmsMessage): ProviderResponse {
        val index = random.nextInt(providers.count())
        val provider = providers.toList()[index]
        message.provider = provider.getName()
        val status = provider.sendSms(message)
        //only retry when there is a failed message
        if (status is ProviderResponse.FAILED && providers.count() > 1) {
            var nextIndex: Int
            do {
                nextIndex = random.nextInt(providers.count())
            } while (nextIndex == index)
            val provider = providers.toList()[nextIndex]
            message.provider = provider.getName()
            log.debug("retrying message with different provider {}", provider.getName())
            return provider.sendSms(message)
        } else {
            return status
        }
    }

    /**
     * If there are mo providers configured then quit the application
     */
    @PostConstruct
    fun init() {
        if (providers.count() == 0) {
            log.error { "******* Application is quitting as at least one SMS Provider must be configured ********" }
            exitProcess(1)
        }
        providers.forEach { log.debug { "Provider: $it has been configured to send messages" } }
    }
}
