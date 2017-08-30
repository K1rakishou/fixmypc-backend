package com.kirakishou.backend.fixmypc.extension

import com.hazelcast.core.HazelcastInstance

inline fun HazelcastInstance.doInTransaction(func: () -> Unit) {
    val context = this.newTransactionContext()
    context.beginTransaction()

    try {
        func()
        context.commitTransaction()
    } catch (e: Throwable) {
        context.rollbackTransaction()
        throw e
    }
}