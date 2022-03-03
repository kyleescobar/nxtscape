package dev.nxtscape.client

import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.Tlhelp32.TH32CS_SNAPPROCESS
import com.sun.jna.ptr.IntByReference
import org.tinylog.kotlin.Logger
import java.io.File
import java.nio.ByteBuffer

object Injector {

    fun getProcessId(processName: String): Int {
        val snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, WinDef.DWORD(0))
        val procEntry = Tlhelp32.PROCESSENTRY32()

        if(!Kernel32.INSTANCE.Process32First(snapshot, procEntry)) {
            return -1
        }

        while(Kernel32.INSTANCE.Process32Next(snapshot, procEntry)) {
            if(Native.toString(procEntry.szExeFile) == processName) {
                Kernel32.INSTANCE.CloseHandle(snapshot)
                return procEntry.th32ProcessID.toInt()
            }
        }

        Kernel32.INSTANCE.CloseHandle(snapshot)
        return -1
    }

    fun injectDLL(processName: String, dllFile: File) = injectDLL(getProcessId(processName), dllFile)

    fun injectDLL(processId: Int, dllFile: File) {
        Logger.info("Injecting DLL (${dllFile.path})...")

        val dllPath = dllFile.absolutePath.toString() + "\u0000"
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

        Logger.info("Successfully injected DLL into process memory.")
    }
}