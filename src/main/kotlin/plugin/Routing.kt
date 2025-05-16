package org.jiezheng.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import org.jiezheng.sequence.Sequencer
import org.jiezheng.sequence.sequenceRouting

fun Application.configureRouting() {
    val serverConfig = environment.config.config("server")
    val capacity = serverConfig.property("capacity").getString().toInt()
    val channelSize = serverConfig.property("channelSize").getString().toInt()
    val sequencer = Sequencer(capacity, channelSize)
    launch {
        sequencer.startProcessor()
    }

    routing {
        get("/") {
            call.respond(HttpStatusCode.NoContent)
        }

        sequenceRouting(sequencer)
    }
}
