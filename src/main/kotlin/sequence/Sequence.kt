package org.jiezheng.sequence

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.Serializable
import org.jiezheng.BadRequest
import org.jiezheng.ServiceUnavailable

internal val LOGGER = KtorSimpleLogger("org.jiezheng.sequence")
private val STUB_PATTERN = Regex("[a-z0-9-_:]+")
private const val STUB_SIZE_MIN: Int = 4
private const val STUB_SIZE_MAX: Int = 64

@Serializable
data class SequenceData(
    val stub: String,
    var value: Int
)

fun Route.sequenceRouting(sequencer: Sequencer) {
    get("/{stub}") {
        val stub = call.parameters["stub"]!!
        if (!validateStub(stub)) {
            throw BadRequest("Invalid stub format")
        }

        val sequenceChannel: Channel<out Int>? = sequencer.getChannel(stub)
        if (sequenceChannel == null) {
            val tempChannel = Channel<Int>()
            sequencer.enqueue(stub, tempChannel)
            try {
                val seq = tempChannel.receive()
                call.respond(SequenceData(stub, seq))
            } catch (e: ClosedReceiveChannelException) {
                throw ServiceUnavailable("The server has reached its max capacity.")
            }
        } else {
            val seq = sequenceChannel.receive()
            call.respond(SequenceData(stub, seq))
        }
    }
}

private fun validateStub(stub: String): Boolean {
    if (stub.length < STUB_SIZE_MIN || stub.length > STUB_SIZE_MAX) {
        return false
    }
    return STUB_PATTERN.matches(stub)
}
