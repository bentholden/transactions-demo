package no.holden.kafkatansactions.kafka

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.db.RecordEntity
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.processor.Punctuator
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class JpaProcessSupplier(
    private val kafkaRecordRepository: KafkaRecordRepository
) : ProcessorSupplier<UUID, String, UUID, String> {
    override fun get(): Processor<UUID, String, UUID, String> = JpaProcess(kafkaRecordRepository)

    private class JpaProcess(
        private val kafkaRecordRepository: KafkaRecordRepository
    ) : Processor<UUID, String, UUID, String> {

        private lateinit var context: ProcessorContext<UUID, String>

        override fun init(context: ProcessorContext<UUID, String>) {
            super.init(context)
            this.context = context
        }

        override fun process(record: Record<UUID, String>) {
            kafkaRecordRepository.save(RecordEntity(record.key(), record.value()))
            context.forward(record)
        }
    }
}