package com.kirakishou.backend.fixmypc.core

sealed class Either<out E, out V> {
    data class Error<out E>(val error: E) : Either<E, Nothing>()
    data class Value<out V>(val value: V) : Either<Nothing, V>()

    fun <V> value(value: V): Either<Nothing, V> = Either.Value(value)
    fun <E> error(value: E): Either<E, Nothing> = Either.Error(value)

    fun <V> either(action: () -> V): Either<Exception, V> =
            try { value(action()) } catch (e: Exception) { error(e) }
}