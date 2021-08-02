package com.spotifyVoice

import authorization.authorization_code.com.spotifyVoice.OsCheck
import authorization.authorization_code.com.spotifyVoice.SpeechRecInteractor
import authorization.authorization_code.com.spotifyVoice.mDnsInteractor
import authorization.authorization_code.com.spotifyVoice.mDnsService

import java.io.BufferedReader
import java.io.IOException
import java.lang.reflect.Array.get
import java.net.Inet4Address
import java.util.*
import java.util.concurrent.TimeUnit


class Main : SpeechRecInteractor.MainInter, mDnsInteractor.MainInter{

    var waitingForHotWord = false

    override fun addAddressToLocalMap(address: Pair<String, Inet4Address>) {
        localNetworkMap.putIfAbsent(address.first, address.second)
    }

    override fun removeAddressFromLocalMap(inet: Inet4Address) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun localNetworkMap(): Map<String, Inet4Address> {
        return localNetworkMap
    }

    override fun startFridayRec() {
        if(!waitingForHotWord) {
            start()
        }
    }


    var localNetworkMap = mutableMapOf<String,Inet4Address>()
    val ostype : OsCheck.OSType = OsCheck().operatingSystemType!!
    var speechRec = SpeechRec(this)
    var mdns = mDnsService(this)

    fun start(){
            waitingForHotWord = true
            speechRec.speechResponseListener.ignore = true
            runpython(ostype)
            waitingForHotWord = false
            speechRec.speechHandled = false
            speechRec.speechResponseListener.ignore = false
            try {
                speechRec.startSpeechRecognition()
            } catch (e: IOException) {
                e.printStackTrace()
            }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            var mainClass = Main()

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    print("shutting down")
                    //kill all java processes
                    when (getOs()) {
                        //TODO add macOS and Linux versions
                        OsCheck.OSType.Windows -> runOScommand(mutableListOf("taskkill", "/f", "/im", "java.exe"))
                    }
                }
            })

            mainClass.start()
        }


        fun runpython(ostype: OsCheck.OSType) {
            println("\nlistening for Friday..")
            when (ostype) {
                OsCheck.OSType.MacOS -> runOScommand(
                    mutableListOf(
                        "python3",
                        "porcupine_hotword.py",
                        "--keyword_file_paths",
                        "friday_mac.ppn"
                    )
                )
                OsCheck.OSType.Linux -> runOScommand(
                    mutableListOf(
                        "python3",
                        "porcupine_hotword.py",
                        "--keyword_file_paths",
                        "friday_linux.ppn"
                    )
                )
                OsCheck.OSType.Windows -> runOScommand(
                    mutableListOf(
                        "cmd.exe",
                        "/c",
                        "python",
                        "porcupine_hotword.py",
                        "--keyword_file_paths",
                        "friday_windows.ppn"
                    )
                )
                OsCheck.OSType.Other -> return
            }

            //println("hotword detected!\n")
        }


        fun runOScommand(command: MutableList<String>) {
            val proc = ProcessBuilder(command)
                .directory(null)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            val output: BufferedReader = proc.inputStream.bufferedReader()
            var line: String? = output.readLine()
            while (line != null) {
                println(line)

                line = output.readLine()
            }

            proc.waitFor(2, TimeUnit.MINUTES)
        }

        fun getOs(): OsCheck.OSType? {
            var detectedOS: OsCheck.OSType? = null
            val operatingSystemType: OsCheck.OSType?
            if (detectedOS == null) {
                val OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
                detectedOS = if (OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0) {
                    OsCheck.OSType.MacOS
                } else if (OS.indexOf("win") >= 0) {
                    OsCheck.OSType.Windows
                } else if (OS.indexOf("nux") >= 0) {
                    OsCheck.OSType.Linux
                } else {
                    OsCheck.OSType.Other
                }
                return detectedOS
            }
            return null
        }
    }
}
