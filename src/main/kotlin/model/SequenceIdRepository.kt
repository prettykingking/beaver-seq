package org.jiezheng.model


interface SequenceIdRepository {
    suspend fun findByStub(stub: String): SequenceId?
    suspend fun add(sequenceId: SequenceId)
    suspend fun update(sequenceId: SequenceId)
}
