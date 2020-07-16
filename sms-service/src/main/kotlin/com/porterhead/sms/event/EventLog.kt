package com.porterhead.sms.event

import mu.KotlinLogging
import java.time.Instant
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

/**
 * Keeps track of events that have been processed
 */
@ApplicationScoped
class EventLog {

    private val log = KotlinLogging.logger{}

    @PersistenceContext
    lateinit var entityManager: EntityManager

    @Transactional(value = Transactional.TxType.MANDATORY)
    fun processed(eventId: UUID) {
        log.debug("event with id {} has been added to the event log", eventId)
        entityManager.persist(ProcessedEvent(eventId, Instant.now()))
    }

    @Transactional(value = Transactional.TxType.MANDATORY)
    fun alreadyProcessed(eventId: UUID): Boolean {
        log.debug("Looking for event with id {} in event log", eventId)
        return entityManager.find(ProcessedEvent::class.java, eventId) != null
    }

}
