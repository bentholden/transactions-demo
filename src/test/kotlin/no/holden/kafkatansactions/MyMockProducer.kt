package no.holden.kafkatansactions

import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.Serializer


class MyMockProducer<K, V>(
    keySerializer: Serializer<K>,
    valueSerializer: Serializer<V>,
    private var totalCommitCount: Int = 0
) : MockProducer<K, V>(true, keySerializer, valueSerializer) {

    var throwOnCommit: MutableMap<Int, Throwable> = HashMap()

    override fun commitTransaction() {
        throwOnCommit[totalCommitCount + 1]
            ?.also {
                throw it
            }

        super.commitTransaction()
        ++totalCommitCount
    }

    fun getTotalCommitCount() = totalCommitCount
}