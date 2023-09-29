package no.holden.kafkatansactions

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.db.RecordEntity
import no.holden.kafkatansactions.kafka.KafkaProducerService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MyService(
    private val kafkaProducerService: KafkaProducerService,
    private val kafkaRecordRepository: KafkaRecordRepository
) {
    @Transactional
    fun sendMessageWithJpaFirst(id: UUID, message: String) {
        kafkaRecordRepository.save(RecordEntity(id, message))
        kafkaProducerService.sendMessage(id, message)
    }

    @Transactional
    fun sendMessageWithKafkaFirst(id: UUID, message: String) {
        kafkaProducerService.sendMessage(id, message)
        kafkaRecordRepository.save(RecordEntity(id, message))
    }
}