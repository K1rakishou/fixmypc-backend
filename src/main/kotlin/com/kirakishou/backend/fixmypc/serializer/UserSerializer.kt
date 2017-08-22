package com.kirakishou.backend.fixmypc.serializer

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.entity.User
import java.sql.Timestamp

class UserSerializer : StreamSerializer<User> {

    override fun read(input: ObjectDataInput): User {
        val user = User()

        user.id = input.readLong()
        user.login = input.readUTF()
        user.password = input.readUTF()
        user.accountType = AccountType.from(input.readByte().toInt())
        user.createdOn = Timestamp(input.readLong())

        return user
    }

    override fun write(output: ObjectDataOutput, user: User) {
        output.writeLong(user.id)
        output.writeUTF(user.login)
        output.writeUTF(user.password)
        output.writeByte(user.accountType.value)
        output.writeLong(user.createdOn!!.time)
    }

    override fun getTypeId(): Int {
        return Constant.HazelcastType.TYPE_USER
    }

    override fun destroy() {
    }
}