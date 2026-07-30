package eu.hxreborn.tfs.action

fun interface Action : AutoCloseable {
    fun execute()

    override fun close() = Unit
}
