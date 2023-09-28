package no.holden.kafkatansactions.kafka

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.db.RecordEntity
import org.springframework.context.annotation.DependsOn
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<UUID, String>,
    private val kafkaRecordRepository: KafkaRecordRepository
) {
    @Transactional
    fun sendMessageWithTransactional(id: UUID, message: String) {
        kafkaRecordRepository.save(RecordEntity(id, message))
        kafkaTemplate.sendDefault(id, message)
    }
}