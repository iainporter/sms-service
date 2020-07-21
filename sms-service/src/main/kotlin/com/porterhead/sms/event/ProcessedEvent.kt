package com.porterhead.sms.event

import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name="sms.processed_event")
data class ProcessedEvent(@Column(nullable = false, name = "event_id") @Id var eventId: UUID,
                          @Column(nullable = false, name = "processed_at") var processedAt: Instant)
