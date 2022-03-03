package dev.nxtscape.client

import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import org.tinylog.kotlin.Logger
import java.io.File
import java.nio.ByteBuffer

object Injector {

    fun injectDLL(file: File, processId: Int) {
        Logger.info("Injecting DLL (${file.path}) into processID: $processId.")

        val dllPath = file.absolutePath.toString() + "\u0000"
        val hProcess = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_ALL_ACCESS, false, processId)
        val pDllPath = Kernel32.INSTANCE.VirtualAllocEx(hProcess, null, BaseTSD.SIZE_T(dllPath.length.toLong()), WinNT.MEM_COMMIT or WinNT.MEM_RESERVE,
            WinNT.PAGE_EXECUTE_READWRITE
        )

        val buf = ByteBuffer.allocateDirect(dllPath.length)
        buf.put(dllPath.toByteArray())
        val bufPtr = Native.getDirectBufferPointer(buf)
        val bytesWritten = IntByReference()

        Kernel32.INSTANCE.WriteProcessMemory(hProcess, pDllPath, bufPtr, dllPath.length, bytesWritten)
        if(dllPath.length != bytesWritten.value) {
            throw RuntimeException("Failed to inject the DLL into the process. Invalid number of bytes were written to memory.")
        }

        val kernel32 = NativeLibrary.getInstance("kernel32")
        val loadLibraryA = kernel32.getFunction("LoadLibraryA")

        val threadId = WinDef.DWORDByReference()
        val hThread = Kernel32.INSTANCE.CreateRemoteThread(hProcess, null, 0, loadLibraryA, pDllPath, 0, threadId)
        Kernel32.INSTANCE.WaitForSingleObject(hThread, WinNT.INFINITE)
        Kernel32.INSTANCE.VirtualFreeEx(hProcess, pDllPath, BaseTSD.SIZE_T(0), WinNT.MEM_RELEASE)
    }
}
