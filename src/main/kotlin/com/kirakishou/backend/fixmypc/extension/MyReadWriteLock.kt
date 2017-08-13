package com.kirakishou.backend.fixmypc.extension

import java.util.concurrent.locks.ReadWriteLock

inline fun ReadWriteLock.lockRead(func: () -> Unit) {
    this.readLock().lock()

    try {
        func()
    } finally {
        this.readLock().unlock()
    }
}

inline fun ReadWriteLock.lockWrite(func: () -> Unit) {
    this.writeLock().lock()

    try {
        func()
    } finally {
        this.writeLock().unlock()
    }
}

inline fun <T> ReadWriteLock.lockReadReturn(func: () -> T): T {
    this.readLock().lock()

    try {
        return func()
    } finally {
        this.readLock().unlock()
    }
}

inline fun <T> ReadWriteLock.lockWriteReturn(func: () -> T): T {
    this.writeLock().lock()

    try {
        return func()
    } finally {
        this.writeLock().unlock()
    }
}