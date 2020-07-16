package com.porterhead.sms.event

import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class ProcessedEvent(@Id var eventId: UUID,
                          var processedAt: Instant)
