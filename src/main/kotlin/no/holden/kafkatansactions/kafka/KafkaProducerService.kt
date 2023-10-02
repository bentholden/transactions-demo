package no.holden.kafkatansactions.kafka

import no.holden.kafkatansactions.db.KafkaOutboxEntry
import no.holden.kafkatansactions.db.KafkaOutboxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<UUID, String>,
    private val kafkaOutboxRepository: KafkaOutboxRepository,
    @Qualifier(value = "kafkaTransactionTemplate") private val kafkaTransactionTemplate: TransactionTemplate,
    @Qualifier(value = "serializableJpaTransactionTemplate") private val serializableJpaTransactionTemplate: TransactionTemplate
) {

    fun addToOutbox(topic: String = "test.topic", id: UUID, message: String) =
        kafkaOutboxRepository.save(
            KafkaOutboxEntry(
                id = id,
                topic = topic,
                payload = message
            )
        )

    @Synchronized
    fun sendMessages() {
        // If there is more than 1 instance of the application running, the execution should only be executed by
        // the leader pod, or there should be functionality to lock the ENTIRE table for reads and writes during
        // the period of producing records (e.g using @Transactional(isolation = Isolation.SERIALIZABLE)
        // in order to maintain the message order. If preserving the message order is not important, this is not necessary.
        var sendException: Throwable? = null
        serializableJpaTransactionTemplate.execute{
            kafkaOutboxRepository.findAllUnsent()
                .sortedBy { it.createdDate }
                .forEach {
                    try {
                        sendMessage(it)
                        kafkaOutboxRepository.save(
                            it.copy(
                                sentDate = OffsetDateTime.now()
                            )
                        )
                    } catch (ex: Exception) {
                        // If a failure occurs, previous successful transactions should not be rolled back but attempted commited.
                        sendException = ex
                        return@execute
                    }
                }
        }

        if (sendException != null) {
            throw sendException!!
        }
    }

    fun sendMessage(kafkaOutboxEntry: KafkaOutboxEntry) =
        // When the producer is transactional, the commit occurs at the end of the transaction block.
        // If the producer however is not transactional, the commit will occur async at a later time.
//        kafkaTransactionTemplate.execute()
        kafkaTransactionTemplate.execute {
            kafkaTemplate.send(kafkaOutboxEntry.topic, kafkaOutboxEntry.id, kafkaOutboxEntry.payload)
                .whenComplete{ result, error ->
                    // Commit errors will not be present here because the transaction block has not ended yet.
                }
        }
}