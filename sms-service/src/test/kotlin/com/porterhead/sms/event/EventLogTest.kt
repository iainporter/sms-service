package com.porterhead.sms.event

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.h2.H2DatabaseTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional

@QuarkusTest
@Transactional
@QuarkusTestResource(value = H2DatabaseTestResource::class)
class EventLogTest {

    @Inject
    lateinit var eventLog: EventLog

    @Test
    fun `event processing`() {
        val eventId = UUID.randomUUID()
        assert(!eventLog.alreadyProcessed(eventId))
        assert(eventLog.alreadyProcessed(eventId))
    }
}
