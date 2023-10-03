package no.holden.kafkatansactions.kafka

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.db.RecordEntity
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class KafkaConsumer(
    private val kafkaRecordRepository: KafkaRecordRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val kafkaTransactionTemplate: TransactionTemplate
) {

    @KafkaListener(topics = ["internal.test.topic"])
    fun onMessage(record: ConsumerRecord<UUID, String>, acknowledgment: Acknowledgment) {
        kafkaTransactionTemplate.execute {
            kafkaRecordRepository.save(RecordEntity(record.key(), record.value()))
            kafkaProducerService.sendMessage("external.text.topic", record.key(), record.value())
            acknowledgment.acknowledge()
        }
    }
}