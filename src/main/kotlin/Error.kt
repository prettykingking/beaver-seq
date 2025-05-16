package org.jiezheng

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class ResponseError(
    val code: Int,
    val message: String
)

class BadRequest(override val message: String) : IllegalArgumentException() {
    val code = HttpStatusCode.BadRequest.value
}

class ServiceUnavailable(override val message: String) : IllegalStateException() {
    val code = HttpStatusCode.ServiceUnavailable.value
}
