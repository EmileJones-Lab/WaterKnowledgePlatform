package top.emilejones.hhu.spliter.exception

class SplitException : Exception {

    constructor(message: String) : super(message)

    constructor(message: String, throwable: Throwable?) : super(message, throwable)
}