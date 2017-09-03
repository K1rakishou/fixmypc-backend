package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class MalfunctionStoreImpl : MalfunctionStore {

    @Autowired
    private lateinit var hazelcast: HazelcastInstance

    private lateinit var malfunctionStore: IMap<Long, Malfunction>

    @PostConstruct
    fun init() {
        malfunctionStore = hazelcast.getMap<Long, Malfunction>(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY)
    }

    override fun saveOne(malfunction: Malfunction) {
        malfunctionStore.put(malfunction.id, malfunction)
    }

    override fun saveMany(malfunctionList: List<Malfunction>) {
        val malfunctionMap = hashMapOf<Long, Malfunction>()

        for (malfunction in malfunctionList) {
            malfunctionMap.put(malfunction.id, malfunction)
        }

        malfunctionStore.putAll(malfunctionMap)
    }

    override fun findOne(malfunctionId: Long): Fickle<Malfunction> {
        val malfunction = malfunctionStore[malfunctionId] ?: return Fickle.empty()
        return Fickle.of(malfunction)
    }

    override fun findMany(malfunctionIdList: List<Long>): List<Malfunction> {
        val set = malfunctionIdList.toSet()
        return ArrayList(malfunctionStore.getAll(set).values)
    }

    override fun deleteOne(malfunctionId: Long) {
        malfunctionStore.remove(malfunctionId)
    }

    override fun deleteMany(malfunctionIdList: List<Long>) {
        malfunctionIdList.forEach {
            malfunctionStore.remove(it)
        }
    }

    override fun clear() {
        malfunctionStore.clear()
    }
}