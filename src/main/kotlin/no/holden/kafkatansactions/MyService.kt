package no.holden.kafkatansactions

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.db.RecordEntity
import no.holden.kafkatansactions.kafka.KafkaProducerService
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class MyService(
    private val kafkaProducerService: KafkaProducerService,
    private val kafkaRecordRepository: KafkaRecordRepository,
    private val defaultJpaTransactionTemplate: TransactionTemplate
) {
    fun processLogic(id: UUID, message: String) {
        defaultJpaTransactionTemplate.execute {
            kafkaProducerService.addToOutbox(id = id, message = message)
            kafkaRecordRepository.save(RecordEntity(id, message))
        }
        kafkaProducerService.sendMessages()
    }
}