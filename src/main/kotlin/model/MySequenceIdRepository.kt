package org.jiezheng.model

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jiezheng.db.SequenceIdEntity
import org.jiezheng.db.SequenceIdTable
import org.jiezheng.db.suspendTransaction

/**
 * SequenceId persistence in MySQL
 */
class MySequenceIdRepository : SequenceIdRepository {
    override suspend fun findByStub(stub: String) : SequenceId? = suspendTransaction {
        SequenceIdEntity.find { (SequenceIdTable.stub eq stub) }
            .limit(1)
            .map(::transform)
            .firstOrNull()
    }

    override suspend fun add(sequenceId: SequenceId) : Unit = suspendTransaction {
        SequenceIdEntity.new {
            stub = sequenceId.stub
            offset = sequenceId.offset
            bucket = sequenceId.bucket
            step = sequenceId.step
            version = sequenceId.version
        }
    }

    override suspend fun update(sequenceId: SequenceId) : Unit = suspendTransaction {
        SequenceIdEntity.findSingleByAndUpdate(SequenceIdTable.stub eq sequenceId.stub) {
            it.offset = sequenceId.offset
            it.version = sequenceId.version
        }
    }

    private fun transform(entity: SequenceIdEntity)  = SequenceId(
        entity.stub,
        entity.offset,
        entity.bucket,
        entity.step,
        entity.version
    )
}
