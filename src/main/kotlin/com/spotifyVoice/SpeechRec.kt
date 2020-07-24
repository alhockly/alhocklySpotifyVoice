package com.spotifyVoice

import authorization.authorization_code.com.spotifyVoice.Online
import authorization.authorization_code.com.spotifyVoice.SpeechRecInteractor
import com.darkprograms.speech.microphone.Microphone
import com.darkprograms.speech.recognizer.GSpeechDuplex
import net.sourceforge.javaflacencoder.FLACFileWriter
import java.io.IOException
import java.lang.IllegalArgumentException
import javax.sound.sampled.LineUnavailableException
import kotlin.system.exitProcess


class SpeechRec(mainClassInterator : SpeechRecInteractor.MainInter) : SpeechRecInteractor.SpeechInter {
    private  var  mic : Microphone
    val duplex = GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw")

    var main = mainClassInterator
    var spotify = Spotify()
    var speechHandled = false
    var speechRecThread : Thread? = null
    var speechRecTimeoutThread = speechRecTimeout(duplex,this,mainClassInterator)
    var speechResponseListener = SpeechResponseListener(speechRecTimeoutThread,this, mainClassInterator)

    init{
        try{
            mic = Microphone(FLACFileWriter.FLAC)
        } catch (e : IllegalArgumentException){
            println("no compatible mic found")
            println("(PCM_SIGNED 8000.0 Hz, 16 bit, mono, 2 bytes/frame, little-endian)")
            exitProcess(-1)
        }
        //Duplex Configuration
        duplex.language = "en"
        duplex.addResponseListener(speechResponseListener)

    }



    fun handleSpeech(text : String){
        var words = text.trim().split(" ")
        speechResponseListener.ignore = true
        //search for a track
        if (text.contains("play") && text.contains("by")){
           spotifyPlayTrack(text)
            speechHandled = true
            return
        }

        //search artists could also be used for albums and playlists
        if(text.contains("play") && text.contains("on spotify")){
            var term = text.substring(text.indexOf("play")+5, text.indexOf("on spotify"))
            var search = spotify.requestGenericSearch("artist,album",term)
            if(search != null){
                spotify.requestPlayTrack(search)
            }
            speechHandled = true
            return
        }

        if(text.contains("play spotify") || text.contains("pause spotify")){
            spotify.requestPausePlayback(text.contains("play"))
            speechHandled = true
            return
        }



        if((text.contains("switch") || text.contains("turn")) && (text.contains("on") || text.contains("off")) && text.contains("lights")){
            var networkMap = main.localNetworkMap()
            var lightsAddress =  networkMap.get("lights")
            var switch = if(text.contains("on")){
                "/on"
            }else{
                "/off"
            }

            if(lightsAddress != null){
                var link = "http://"+lightsAddress.hostAddress.replace("/","")+ switch
                Online().wget(link)
            } else {
                print("lights ip address could not be resolved")
            }
            speechHandled = true
            return
        }

        speechHandled = true
    }



    fun spotifyPlayTrack(text : String){
        var inputsong = text.substring(text.indexOf("play ") + 5, text.indexOf(" by "))
        var inputartist = text.substring(text.indexOf( " by ") + 3)

        var topResult = spotify.requestSearchTrack("$inputsong $inputartist")
        //TODO get list here and calc leven for condtion for if below
        if(topResult!= null) {
            spotify.requestPlayTrack(topResult)
        }else{
            println("\nno matching track found in spotify search")
            //TODO search spoify using only first syllabel for each for of input
            var int =  inputsong.split(" ")
            inputsong = ""
            for(s in int){
                if(s.length == 1){ continue}
                inputsong += s.substring(0,2)+" "
            }
            var artist = inputartist.trim().split(" ")
            inputartist = ""
            for(a in artist){
                if(a.length == 1){ continue}
                inputartist += a.substring(0,2)+" "
            }
            inputsong = inputsong.trim()
            inputartist = inputartist.trim()
            var firstSyllableSearch = spotify.requestSearchTrack("$inputsong $inputartist")
            if(firstSyllableSearch != null) {
                spotify.requestPlayTrack(firstSyllableSearch)
            }
            else{
                spotify.roughGoogle("$inputsong$inputartist")
            }
        }
    }


    fun startSpeechRecognition() {
        duplex.stopSpeechRecognition()
        val threadSet = Thread.getAllStackTraces()
        for( thread in Thread.getAllStackTraces().keys){
            if(thread.name.equals("Downstream Thread")){
                thread.stop()
            }
        }

        //Start a new Thread so our application don't lags
        speechRecThread = Thread {
            //println("\nspeech rec thread open\n")
            try {
                duplex.recognize(mic.targetDataLine, mic.audioFormat)


            } catch (e: LineUnavailableException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e : IOException){
                e.printStackTrace()
            } catch (e : Exception){

            }

        }


        print("")

        speechRecThread!!.start()
        speechRecTimeoutThread.run()

    }

    /**
     * Stops the Speech Recognition
     */
    fun stopSpeechRecognition() {
        duplex.stopSpeechRecognition()
     //   duplex.removeResponseListener(speechResponseListener)
        mic.close()
        speechRecThread!!.stop()

       // println("mic closed, long speech rec thread closed")
       // println("speechRecThread already alive? ${speechRecThread!!.isAlive}")
        //println("\nStopping Speech Recognition...." + " , Microphone State is:" + mic.state)

    }


    class speechRecTimeout(Gspeech : GSpeechDuplex, speechRec: SpeechRec, mainClass: SpeechRecInteractor.MainInter) : Runnable {
        val duplex = Gspeech
        val speechRecClass = speechRec
        var cancel = false

        var main = mainClass

        override fun run() {
            var count = 6
            for (i in 0..count){
                //println(i)
                Thread.sleep(1000)
                if(cancel){
                    return
                }
            }

            duplex.stopSpeechRecognition()
            speechRecClass.speechHandled = true
            Thread.sleep(1000)
            println("\nspeech rec timed out")
            main.startFridayRec()
        }
    }
}

