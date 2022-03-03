package dev.nxtscape.client

import dev.nxtscape.client.Updater.crc
import org.tinylog.kotlin.Logger
import java.io.File
import java.util.zip.CRC32

object Updater {

    fun run() {
        Logger.info("Checking for updates...")

        val baseDir = File(System.getProperty("user.home")).resolve("AppData/Local/nxtscape/")
        val binDir = File(object {}.javaClass.getResource("/bin/")!!.file)
        binDir.walk().forEach {
            val path = it.relativeTo(binDir)
            if(it.isFile) {
                val file = baseDir.resolve(path.path)
                if(file.exists()) {
                    val oldCrc = it.readBytes().crc()
                    val latestCrc = file.readBytes().crc()
                    if(latestCrc != oldCrc) {
                        it.update(file)
                    }
                } else {
                    it.update(file)
                }
            } else {
                val dir = baseDir.resolve(path.path)
                if(!dir.exists()) {
                    dir.mkdirs()
                }
            }
        }

        Logger.info("Client installation is up-to-date.")
    }

    private fun File.update(file: File) {
        Logger.info("Updating client file: ${this.name}.")
        if(file.exists()) file.deleteRecursively()
        file.outputStream().use { it.write(this.readBytes()) }
    }

    private fun ByteArray.crc(): Long {
        val crc = CRC32()
        crc.update(this)
        return crc.value
    }
}