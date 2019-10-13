package cdmp.app.datatype

import cdmp.app.datatype.Either.Companion.left
import cdmp.app.datatype.Either.Companion.right
import java.lang.Exception


fun <R> R?.getOrThrowable(): Either<Throwable, R> = if (this != null) right(this) else left(Exception("null value"))

sealed class Either<out L, out R> {

    companion object {
        fun <L> left(a: L) = Left(a)
        fun <R> right(b: R) = Right(b)
    }

    data class Left<out L>(val a: L) : Either<L, Nothing>()
    data class Right<out R>(val b: R) : Either<Nothing, R>()

    inline fun <C> fold(fnL: (L) -> C, fnR: (R) -> C): C =
        when (this) {
            is Left -> fnL(a)
            is Right -> fnR(b)
        }

    fun whenRight(fn: (R) -> Unit) {
        when (this) {
            is Left -> {
            }
            is Right -> fn(b)
        }
    }

    fun whenLeft(fn: (L) -> Unit) {
        when (this) {
            is Left -> fn(a)
            is Right -> {
            }
        }
    }
}


inline fun <T, L, R> Either<L, R>.flatMap(fn: (R) -> Either<L, T>): Either<L, T> =
    when (this) {
        is Either.Left -> Either.Left(a)
        is Either.Right -> fn(b)
    }

fun <T, L, R> Either<L, R>.map(fn: (R) -> (T)): Either<L, T> = this.flatMap { right(fn(it)) }

fun <P, T, L, R> Either<P, T>.mapLeft(
    leftFn: (P) -> L
) = bimap({ leftFn(it) }, { it })

fun <P, T, L, R> Either<P, T>.bimap(
    leftFn: (P) -> L,
    rightFn: (T) -> R
): Either<L, R> = when (this) {
    is Either.Right -> Either.Right(rightFn(b))
    is Either.Left -> Either.Left(leftFn(a))
}

inline fun <L, R> safeCall(
    errorMapper: (Throwable) -> L,
    block: () -> R
): Either<L, R> =
    try {
        val response = block()
        right(response)
    } catch (exception: Exception) {
        left(errorMapper(exception))
    }

inline fun <R> safeCall(
    block: () -> R
): Either<Throwable, R> =
    safeCall({ it }, block)
