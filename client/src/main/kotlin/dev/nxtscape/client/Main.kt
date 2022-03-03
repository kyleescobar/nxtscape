package dev.nxtscape.client

import org.tinylog.kotlin.Logger
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Main {

    private var processId: Int = -1
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

        Runtime.getRuntime().exec(arrayOf("cmd", "/c", baseDir.resolve("nxtscape.exe").absolutePath))

        val startTime = System.currentTimeMillis()
        while(true) {
            if(System.currentTimeMillis() - startTime >= 30000L) {
                Logger.error("Failed to start client process within the 30 second timeout.")
                exitProcess(0)
            }

            val proc = Runtime.getRuntime()
                .exec(arrayOf("cmd", "/c", "tasklist /FI \"IMAGENAME eq nxtscape.exe\""))
            proc.waitFor()
            val lines = proc.inputStream.let { InputStreamReader(it) }.readText().split("\n")
            if(lines.any { it.contains("nxtscape.exe") }) {
                break
            }
        }
    }

    private fun inject() {
        Injector.injectDLL("nxtscape.exe", baseDir.resolve("nxtscape.dll"))
    }
}