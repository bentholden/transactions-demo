package no.holden.kafkatansactions

import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.UUIDSerializer
import java.util.UUID

class MockProducerService {

    var mockProducer: MyMockProducer<UUID, String>? = null

    private var commitException: RuntimeException? = null
    private val throwOnCommit: MutableMap<Int, Throwable> = HashMap()

    fun createMockProducer(tx: Boolean, id: String): MockProducer<UUID, String> {
        if(mockProducer == null || mockProducer?.closed() == true) {
            mockProducer = MyMockProducer(UUIDSerializer(), StringSerializer(), mockProducer?.getTotalCommitCount() ?: 0)
                .apply {
                    initTransactions()
                }
            setExceptons(mockProducer!!)
        }

        return mockProducer!!
    }

    fun setCommitException(commitException: RuntimeException?, commitNumber: Int? = null) {
        if (commitNumber != null) {
            if(commitException != null) {
                throwOnCommit[commitNumber] = commitException
            } else {
                throwOnCommit.remove(commitNumber)
            }
        } else {
            this.commitException = commitException
        }

        if (mockProducer != null) {
            setExceptons(mockProducer!!)
        }
    }

    fun closeCurrentProducer() {
        if (mockProducer != null) {
            mockProducer!!.close()
            mockProducer = null
        }
    }

    private fun setExceptons(mockProducer: MyMockProducer<UUID, String>) {
        mockProducer.commitTransactionException = commitException
        mockProducer.throwOnCommit = throwOnCommit
    }
}