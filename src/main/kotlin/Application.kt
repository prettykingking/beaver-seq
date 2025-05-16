package org.jiezheng

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jiezheng.plugin.configureDatabases
import org.jiezheng.plugin.configureHTTP
import org.jiezheng.plugin.configureRouting
import org.jiezheng.plugin.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // configureMonitoring()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting()

    install(StatusPages) {
        exception<BadRequest> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ResponseError(cause.code, cause.message))
        }
        exception<ServiceUnavailable> { call, cause ->
            call.respond(HttpStatusCode.ServiceUnavailable, ResponseError(cause.code, cause.message))
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
}
