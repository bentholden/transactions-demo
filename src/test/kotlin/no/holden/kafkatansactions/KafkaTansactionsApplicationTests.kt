package no.holden.kafkatansactions

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.kafka.KafkaProducerService
import org.apache.kafka.clients.producer.MockProducer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureTestDatabase
@Import(value = [MockProducerFactoryConfig::class])
@EmbeddedKafka(partitions = 1, topics = ["test.topic"], controlledShutdown = true)
class KafkaTansactionsApplicationTests {

    @Autowired
    private lateinit var kafkaProducerService: KafkaProducerService;

    @Autowired
    private lateinit var kafkaRecordRepository: KafkaRecordRepository

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var mockProducer: MockProducer<UUID, String>

    @Test
    fun contextLoads() {
        mockProducer.commitTransactionException = RuntimeException("Oh no! a commit exception!!")

        val id = UUID.randomUUID()
        val message = "hello"

        assertThrows<RuntimeException> {
            kafkaProducerService.sendMessageWithTransactional(id, message)
        }

        val dbRecord = kafkaRecordRepository.findByIdOrNull(id)

        assertNotNull(dbRecord)
        assertEquals(message, dbRecord.record)
    }
}
