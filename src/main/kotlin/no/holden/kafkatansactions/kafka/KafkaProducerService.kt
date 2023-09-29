package no.holden.kafkatansactions.kafka

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.db.RecordEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<UUID, String>,
    private val kafkaRecordRepository: KafkaRecordRepository
) {
    fun sendMessage(id: UUID, message: String) {
        kafkaTemplate.sendDefault(id, message)
    }
}