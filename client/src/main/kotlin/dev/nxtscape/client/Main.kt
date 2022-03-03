package dev.nxtscape.client

import org.tinylog.kotlin.Logger
import java.io.File
import java.util.concurrent.TimeUnit

object Main {

    private lateinit var process: Process
    private val baseDir = File(System.getProperty("user.home")).resolve("AppData/Local/nxtscape/")

    @JvmStatic
    fun main(args: Array<String>) {
        Updater.run()
        start()
        inject()
        Logger.info("Successfully started client.")
    }

    private fun start() {
        Logger.info("Starting client process...")
        process = ProcessBuilder(baseDir.resolve("nxtscape.exe").absolutePath).start()
        process.waitFor(500L, TimeUnit.MILLISECONDS)
    }

    private fun inject() {
        Injector.injectDLL(baseDir.resolve("nxtscape.dll"), process.pid().toInt())
    }
}