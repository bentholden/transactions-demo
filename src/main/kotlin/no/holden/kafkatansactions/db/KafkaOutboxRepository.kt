package no.holden.kafkatansactions.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KafkaOutboxRepository : JpaRepository<KafkaOutboxEntry, UUID> {

    @Query(
        value = """
            SELECT * FROM T_KAFKA_OUTBOX
            WHERE SENT_DATE IS NULL
        """,
        nativeQuery = true
    )
    fun findAllUnsent(): List<KafkaOutboxEntry>

}