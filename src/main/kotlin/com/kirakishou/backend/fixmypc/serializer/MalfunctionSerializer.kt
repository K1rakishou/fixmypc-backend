package com.kirakishou.backend.fixmypc.serializer

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import java.sql.Timestamp

class MalfunctionSerializer : StreamSerializer<Malfunction> {

    override fun write(output: ObjectDataOutput, malfunction: Malfunction) {
        output.writeLong(malfunction.id)
        output.writeLong(malfunction.ownerId)
        output.writeBoolean(malfunction.isActive)
        output.writeUTF(malfunction.malfunctionRequestId)
        output.writeInt(malfunction.category)
        output.writeUTF(malfunction.description)
        output.writeDouble(malfunction.lat)
        output.writeDouble(malfunction.lon)
        output.writeLong(malfunction.createdOn!!.time)
        output.writeInt(malfunction.imageNamesList.size)

        for (image in malfunction.imageNamesList) {
            output.writeUTF(image)
        }
    }

    override fun getTypeId(): Int {
        return Constant.HazelcastType.TYPE_MALFUNCTION
    }

    override fun read(input: ObjectDataInput): Malfunction {
        val malfunction = Malfunction()

        malfunction.id = input.readLong()
        malfunction.ownerId = input.readLong()
        malfunction.isActive = input.readBoolean()
        malfunction.malfunctionRequestId = input.readUTF()
        malfunction.category = input.readInt()
        malfunction.description = input.readUTF()
        malfunction.lat = input.readDouble()
        malfunction.lon = input.readDouble()
        malfunction.createdOn = Timestamp(input.readLong())

        val imagesCount = input.readInt()
        val images = arrayListOf<String>()

        for (i in 0 until imagesCount) {
            images.add(input.readUTF())
        }

        malfunction.imageNamesList = images

        return malfunction
    }

    override fun destroy() {
    }
}