package org.jiezheng.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


object SequenceIdTable : IntIdTable("sequence_id", "added_id") {
    val stub = varchar("stub", 64)
    val offset = integer("offset")
    val bucket = short("bucket")
    val step = byte("step")
    val version = integer("version")
}

class SequenceIdEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SequenceIdEntity>(SequenceIdTable)

    var stub by SequenceIdTable.stub
    var offset by SequenceIdTable.offset
    var bucket by SequenceIdTable.bucket
    var step by SequenceIdTable.step
    var version by SequenceIdTable.version
}
