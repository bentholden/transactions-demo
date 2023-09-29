package no.holden.kafkatansactions.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KafkaRecordRepository : JpaRepository<RecordEntity, UUID>