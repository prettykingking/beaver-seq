package org.jiezheng.model

import kotlinx.serialization.Serializable

/**
 * In most scenarios, sequence start from 1 by 1 to the max.
 */
object SequenceDefault {
    const val OFFSET: Int = 1
    const val BUCKET: Short = 100
    const val STEP: Byte = 1
}

@Serializable
data class SequenceId(
    val stub: String,
    val offset: Int,
    val bucket: Short,
    val step: Byte,
    val version: Int,
)
