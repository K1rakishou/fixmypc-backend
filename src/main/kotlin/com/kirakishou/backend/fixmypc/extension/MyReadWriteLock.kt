package com.kirakishou.backend.fixmypc.extension

import java.util.concurrent.locks.ReadWriteLock

fun <T> ReadWriteLock.lockAndRead(func: () -> T): T {
    this.readLock().lock()

    try {
        return func()
    } finally {
        this.readLock().unlock()
    }
}

fun <T> ReadWriteLock.lockAndWrite(func: () -> T): T {
    this.writeLock().lock()

    try {
        return func()
    } finally {
        this.writeLock().unlock()
    }
}