package org.jiezheng.sequence

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * Produces [SequenceBarrier] that sends sequence to channel in coroutine.
 *
 * The Sequencer works in single-thread context.
 */
class Sequencer(private val capacity: Int, private val channelSize: Int) {
    /**
     * Store sequence channel in cache.
     */
    private val sequenceChannelCache = HashMap<String, Channel<out Int>>(capacity, 1.0f)

    /**
     * The buffered channel for [SequenceBarrier] to be created.
     */
    private val barrierChannel = Channel<Pair<String, Channel<in Int>>>(channelSize)

    /**
     * Get sequence channel from cache.
     */
    fun getChannel(stub: String): Channel<out Int>? = sequenceChannelCache[stub]

    suspend fun enqueue(stub: String, tempChannel: Channel<in Int>) = coroutineScope {
        barrierChannel.send(stub to tempChannel)
    }

    /**
     * Start barrier processor. Only one processor exists during application lifecycle,
     * one coroutine write, then others read. So it's thread-safe.
     */
    suspend fun startProcessor() = coroutineScope {
        for (pair in barrierChannel) {
            if (sequenceChannelCache.size == capacity) {
                pair.second.close() // cache is full, decline new stub
                LOGGER.info("cache is full, decline new stub")
                continue
            }

            val sequenceChannel = sequenceChannelCache[pair.first]
            var seq: Int

            if (sequenceChannel == null) {
                val channel = Channel<Int>(channelSize)
                val barrier = SequenceBarrier(pair.first, channel)
                launch {
                    barrier.start()
                }

                // save channel in cache
                sequenceChannelCache[pair.first] = channel
                seq = channel.receive()
            } else {
                seq = sequenceChannel.receive()
            }

            pair.second.send(seq)
            // close the channel explicitly as it's only used once from the sender view.
            // subsequent sequence can be received through cached channel.
            pair.second.close()
        }
    }
}
