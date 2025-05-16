package org.jiezheng.sequence

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import org.jiezheng.model.MySequenceIdRepository
import org.jiezheng.model.SequenceDefault
import org.jiezheng.model.SequenceId


/**
 * SequenceBarrier constantly send sequence to the buffered channel,
 * until sequence reaches limit ([Integer.MAX_VALUE] or [Long.MAX_VALUE]).
 *
 * @param stub Sequence stub identifier
 * @param sequenceChannel Buffered channel to send sequences
 */
class SequenceBarrier(private val stub: String, private val sequenceChannel: Channel<in Int>) {
    /**
     * SequenceIdRepository interface
     */
    private val sequenceIdRepository = MySequenceIdRepository()

    suspend fun start(): Unit = coroutineScope {
        var sequenceId: SequenceId? = nextBucket()
        while (sequenceId != null) {
            val first = sequenceId.offset
            val last = sequenceId.offset + sequenceId.bucket
            LOGGER.info("sequence bucket: $stub, $first, $last")
            for (seq in first..<last) {
                sequenceChannel.send(seq)
            }
            sequenceId = nextBucket()
        }

        sequenceChannel.close() // no more sequences, close channel
    }

    private suspend fun nextBucket(): SequenceId? {
        val current : SequenceId? = sequenceIdRepository.findByStub(stub)
        if (current == null) {
            val sequenceId = SequenceId(stub,
                SequenceDefault.OFFSET,
                SequenceDefault.BUCKET,
                SequenceDefault.STEP,
                1)
            sequenceIdRepository.add(sequenceId)
            return sequenceId
        } else {
            val offset = current.offset + current.bucket
            if (offset < 0) { // max Int overflow
                return null
            }

            val next = SequenceId(stub,
                offset,
                current.bucket,
                current.step,
                current.version + 1)
            sequenceIdRepository.update(next)
            return current
        }
    }
}
