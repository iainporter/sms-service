package com.porterhead.sms.event

import mu.KotlinLogging
import java.lang.Exception
import java.time.Instant
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityExistsException
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceException
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
    fun alreadyProcessed(eventId: UUID): Boolean {
        log.debug("Looking for event with id {} in event log", eventId)
        try {
            entityManager.persist(ProcessedEvent(eventId, Instant.now()))
            entityManager.flush()
        } catch (e: EntityExistsException) {
            log.debug("event with id {} has been already been processed", eventId)
            return true
        }
        log.debug("event with id {} has been added to the event log", eventId)
        return false
    }

}
