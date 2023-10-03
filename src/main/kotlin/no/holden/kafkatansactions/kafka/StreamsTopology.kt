package no.holden.kafkatansactions.kafka

import org.apache.kafka.streams.StreamsBuilder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class StreamsTopology(
    streamsBuilder: StreamsBuilder,
    jpaProcessSupplier: JpaProcessSupplier
) {

    init {
        streamsBuilder.stream<UUID, String>("internal.test.topic")
            .process(jpaProcessSupplier)
            .to("external.test.topic")
    }
}