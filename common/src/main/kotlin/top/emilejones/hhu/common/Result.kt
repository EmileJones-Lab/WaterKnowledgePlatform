package top.emilejones.hhu.common

/**
 * 通用的结果包装类，旨在提供良好的 Java 与 Kotlin 互操作性。
 */
class Result<T> private constructor(
    private val value: T?,
    private val throwable: Throwable?,
    val isSuccess: Boolean
) {
    val isFailure: Boolean get() = !isSuccess

    fun getOrNull(): T? = value

    fun exceptionOrNull(): Throwable? = throwable

    /**
     * 获取异常，如果结果为成功则抛出异常。
     */
    fun exceptionOrThrow(): Throwable {
        if (isFailure) return throwable!!
        throw RuntimeException("Result is success, no exception found")
    }

    /**
     * 获取结果，如果失败则抛出异常。
     */
    fun getOrThrow(): T {
        if (isSuccess) return value as T
        throw throwable ?: RuntimeException("Unknown error")
    }

    companion object {
        @JvmStatic
        fun <T> success(value: T): Result<T> = Result(value, null, true)

        @JvmStatic
        fun successVoid(): Result<Void> = Result(null, null, true)

        @JvmStatic
        fun <T> failure(throwable: Throwable): Result<T> = Result(null, throwable, false)
    }
}

/**
 * 将 Kotlin 标准库的 Result 转换为自定义的 Result。
 */
fun <T> kotlin.Result<T>.toCommonResult(): Result<T> {
    return if (this.isSuccess) {
        Result.success(this.getOrThrow())
    } else {
        Result.failure(this.exceptionOrNull()!!)
    }
}

/**
 * 将 Kotlin 标准库的 Result<Unit> 转换为自定义的 Result<Void>。
 */
fun<T> kotlin.Result<T>.toCommonVoidResult(): Result<Void> {
    return if (this.isSuccess) {
        Result.successVoid()
    } else {
        Result.failure(this.exceptionOrNull()!!)
    }
}

