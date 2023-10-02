package no.holden.kafkatansactions.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "T_KAFKA_OUTBOX")
data class KafkaOutboxEntry(
    @Id
    val id: UUID,
    val topic: String,
    val payload: String,
    @Column(name = "CREATED_DATE")
    val createdDate: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "SENT_DATE")
    val sentDate: OffsetDateTime? = null,
)