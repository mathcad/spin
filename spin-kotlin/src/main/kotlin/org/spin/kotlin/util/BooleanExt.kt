package org.spin.kotlin.util

/**
 * Boolean流式扩展
 * <p>Created by xuweinan on 2018/2/12.</p>
 *
 * @author xuweinan
 */
sealed class BooleanExt<out T> constructor(val boolean: Boolean)

object Otherwise : BooleanExt<Nothing>(true)
class WithData<out T>(val data: T) : BooleanExt<T>(false)

inline fun <T> Boolean.yes(block: () -> T): BooleanExt<T> = when {
    this -> {
        WithData(block())
    }
    else -> Otherwise
}

inline fun <T> Boolean.no(block: () -> T) = when {
    this -> Otherwise
    else -> {
        WithData(block())
    }
}

inline infix fun <T> BooleanExt<T>.otherwise(block: () -> T): T {
    return when (this) {
        Otherwise -> block()
        is WithData<T> -> this.data
    }
}

inline operator fun <T> Boolean.invoke(block: () -> T) = yes(block)
