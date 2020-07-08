package com.spotifyVoice

import java.io.IOException
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Compiler.command
import java.util.concurrent.TimeUnit


class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var speechRec = SpeechRec()
          //  var t = Spotify().urlToUri("https://open.spotify.com/track/5hqh0JUxRShhqdaxu7wlz5?si=9Z98g3YoRK-ZeG7ujsf4kw")

            print("running main\n")
            while (true) {
                speechRec.speechResponseListener.ignore=true
                Thread.sleep(200)
                runpython()

                speechRec.speechHandled = false
                speechRec.speechResponseListener.ignore=false
                speechRec.startSpeechRecognition()
                while(!speechRec.speechHandled){
                    //do nothing
                }
            }



        }

        fun runpython() {
            println("\nlistening for Friday..")
            runcommandv3(mutableListOf("cmd.exe", "/c", "python", "porcupine_hotword.py", "--keyword_file_paths", "friday_windows.ppn"))
            //println("hotword detected!\n")
        }

        fun runcommandv3(command : MutableList<String>){
            val proc = ProcessBuilder(command)
                .directory(null)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            val output: BufferedReader = proc.inputStream.bufferedReader()
            var line: String? = output.readLine()
            val log: File = File("cmd.log")
            while (line != null) {
                println(line)
                log.appendText(line.toString() + System.lineSeparator())
                line = output.readLine()
            }

            proc.waitFor(2, TimeUnit.MINUTES)
        }

    }
}