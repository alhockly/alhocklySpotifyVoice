package com.spotifyVoice

import authorization.authorization_code.com.spotifyVoice.SpeechRecInteractor
import authorization.authorization_code.com.spotifyVoice.mDnsInteractor
import authorization.authorization_code.com.spotifyVoice.mDnsService
import java.io.IOException
import java.io.BufferedReader
import java.io.File
import java.net.Inet4Address
import java.util.concurrent.TimeUnit


class Main : SpeechRecInteractor.MainInter, mDnsInteractor.MainInter{
    override fun addAddressToLocalMap(address: Pair<String, Inet4Address>) {
        localNetworkMap.putIfAbsent(address.first, address.second)
    }

    override fun removeAddressFromLocalMap(inet: Inet4Address) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun localNetworkMap(): Map<String, Inet4Address> {
        return localNetworkMap
    }


    var localNetworkMap = mutableMapOf<String,Inet4Address>()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //TODO create spotify object here (and create interface)?
            var main = Main()
            var speechRec = SpeechRec(main)
            var mdns = mDnsService(main)

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    print("shutting down")
                    //TODO if windows
                    runOScommand(mutableListOf("taskkill", "/f", "/im", "java.exe"))
                }
            })


            print("running main\n")
            while (true) {
                speechRec.speechResponseListener.ignore=true
                runpython()

                speechRec.speechHandled = false
                speechRec.speechResponseListener.ignore=false
                try{
                    speechRec.startSpeechRecognition()
                } catch (e : IOException){
                    e.printStackTrace()
                }

                while(!speechRec.speechHandled){
                    //do nothing
                }
            }



        }

        fun runpython() {
            println("\nlistening for Friday..")
            runOScommand(mutableListOf("cmd.exe", "/c", "python", "porcupine_hotword.py", "--keyword_file_paths", "friday_windows.ppn"))
            //println("hotword detected!\n")
        }

        fun runOScommand(command : MutableList<String>){
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

    }
}