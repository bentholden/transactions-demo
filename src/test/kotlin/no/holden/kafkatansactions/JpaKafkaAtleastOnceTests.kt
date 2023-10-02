package no.holden.kafkatansactions

import no.holden.kafkatansactions.db.KafkaOutboxEntry
import no.holden.kafkatansactions.db.KafkaOutboxRepository
import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.lang.IllegalStateException
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@AutoConfigureTestDatabase
@Import(value = [MockProducerFactoryConfig::class])
@EmbeddedKafka(partitions = 1, topics = ["test.topic"], controlledShutdown = true)
class JpaKafkaAtleastOnceTests {

    @Autowired
    private lateinit var myService: MyService

    @Autowired
    private lateinit var kafkaRecordRepository: KafkaRecordRepository

    @Autowired
    private lateinit var kafkaOutBoxRepository: KafkaOutboxRepository

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var mockProducerService: MockProducerService

    @Autowired
    private lateinit var kafkaProducerService: KafkaProducerService

    @BeforeEach
    fun setup() {
        mockProducerService.closeCurrentProducer()
    }

    @Test
    fun `Jpa entity is saved when kafka commit fails`() {
        mockProducerService.setCommitException(RuntimeException("Oh no! a commit exception!!"))

        val id = UUID.randomUUID()
        val message = "hello"

        assertThrows<RuntimeException> (
            message = "Oh no! a commit exception!!",
        ) { myService.processLogic(id, message) }

        val dbRecord = kafkaRecordRepository.findByIdOrNull(id)

        assertNotNull(dbRecord)
        assertEquals(message, dbRecord.record)

        val failedOutboxEntry = kafkaOutBoxRepository.findByIdOrNull(id)

        assertNotNull(failedOutboxEntry)
        assertNull(failedOutboxEntry.sentDate)
        mockProducerService.setCommitException(null)
        myService.processLogic(id, message)
//
        val succeededOutboxEntry = kafkaOutBoxRepository.findByIdOrNull(id)

        assertNotNull(succeededOutboxEntry)
        assertNotNull(succeededOutboxEntry.sentDate)
    }

    @Test
    fun `2Jpa entity is saved when kafka commit fails`() {
        mockProducerService.setCommitException(RuntimeException("Oh no! a commit exception!!"), 3)
        val messageOne = KafkaOutboxEntry(UUID.randomUUID(), "test.topic", "message 1", OffsetDateTime.now(), null)
        val messageTwo = KafkaOutboxEntry(UUID.randomUUID(), "test.topic", "message 2", OffsetDateTime.now(), null)
        val messageThree = KafkaOutboxEntry(UUID.randomUUID(), "test.topic", "message 3", OffsetDateTime.now(), null)
        kafkaOutBoxRepository.saveAll(listOf(messageOne, messageTwo, messageThree))

        assertThrows<RuntimeException>(
            message = "Oh no! a commit exception!!",
        ) { kafkaProducerService.sendMessages() }

        val processedMessageOne = kafkaOutBoxRepository.findByIdOrNull(messageOne.id)
        assertNotNull(processedMessageOne)
        assertNotNull(processedMessageOne.sentDate)

        val processedMessageTwo = kafkaOutBoxRepository.findByIdOrNull(messageTwo.id)
        assertNotNull(processedMessageTwo)
        assertNotNull(processedMessageTwo.sentDate)

        val processedMessageThree = kafkaOutBoxRepository.findByIdOrNull(messageThree.id)
        assertNotNull(processedMessageThree)
        assertNull(processedMessageThree.sentDate)

        mockProducerService.setCommitException(null, 3)
        kafkaProducerService.sendMessages()

        val processedMessageThreeTakeTwo = kafkaOutBoxRepository.findByIdOrNull(messageThree.id)
        assertNotNull(processedMessageThreeTakeTwo)
        assertNotNull(processedMessageThreeTakeTwo.sentDate)
    }
}
