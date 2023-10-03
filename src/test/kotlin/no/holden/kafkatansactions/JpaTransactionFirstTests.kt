package no.holden.kafkatansactions

import no.holden.kafkatansactions.db.KafkaRecordRepository
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.lang.IllegalStateException
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureTestDatabase
@Import(value = [MockProducerFactoryConfig::class])
@EmbeddedKafka(partitions = 1, topics = ["internal.test.topic", "external.test.topic"], controlledShutdown = true)
class JpaTransactionFirstTests {

    @Autowired
    private lateinit var myService: MyService

    @Autowired
    private lateinit var kafkaRecordRepository: KafkaRecordRepository

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var mockProducer: MockProducer<UUID, String>

    @Test
    fun `Jpa entity is saved when kafka commit fails`() {
        mockProducer.commitTransactionException = RuntimeException("Oh no! a commit exception!!")

        val id = UUID.randomUUID()
        val message = "hello"

        assertThrows<RuntimeException> {
            myService.sendMessageWithJpaFirst(id, message)
        }

        val dbRecord = kafkaRecordRepository.findByIdOrNull(id)

        assertNotNull(dbRecord)
        assertEquals(message, dbRecord.record)

        // Setup kafka consumer
        val consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker)
        consumerProps[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_committed"
        val kafkaConsumer = KafkaConsumer(consumerProps, UUIDDeserializer(), StringDeserializer())
        embeddedKafkaBroker.consumeFromEmbeddedTopics(kafkaConsumer, "test.topic")
        // --------------------

        assertThrows<IllegalStateException>(message = "No records found for topic") {
            val kafkaRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, "test.topic", Duration.ofSeconds(2))
        }
    }
}
