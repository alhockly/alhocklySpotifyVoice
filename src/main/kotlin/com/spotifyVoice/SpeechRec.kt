package com.spotifyVoice

import com.darkprograms.speech.microphone.Microphone
import com.darkprograms.speech.recognizer.GSpeechDuplex
import com.darkprograms.speech.recognizer.GSpeechResponseListener
import com.darkprograms.speech.recognizer.GoogleResponse
import net.sourceforge.javaflacencoder.FLACFileWriter
import javax.sound.sampled.LineUnavailableException
import Online
import com.wrapper.spotify.SpotifyApi
import java.lang.System.exit
import java.net.URI
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionException
import com.wrapper.spotify.SpotifyApiThreading.executeAsync
import java.util.concurrent.CompletableFuture
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest




class SpeechRec {
    private val mic = Microphone(FLACFileWriter.FLAC)
    private val duplex = GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw")

    var spotify = Spotify()
    var speechHandled = false
    var speechRecThread : Thread? = null

    var speechResponseListener = SpeechResponseListener(this)

    init{
        //Duplex Configuration
        duplex.language = "en"
        duplex.addResponseListener(speechResponseListener)
    }



    fun handleSpeech(text : String){
        speechResponseListener.ignore = true
        //search for a track
        if (text.contains("play") && text.contains("by")){
           spotifyPlayTrack(text)
        }

        //search artists could also be used for albums and playlists
        if(text.contains("play") && text.contains("on spotify")){
            var term = text.substring(text.indexOf("play")+5, text.indexOf("on spotify"))
            var search = spotify.requestGenericSearch("artist",term)
            if(search != null){
                spotify.requestPlayTrack(search)
            }
        }

        if(text.contains("play spotify") || text.contains("pause spotify")){
            spotify.requestPausePlayback(text.contains("play"))
        }



        speechHandled = true
    }



    fun spotifyPlayTrack(text : String){
        var inputsong = text.substring(text.indexOf("play ") + 5, text.indexOf(" by "))
        var inputartist = text.substring(text.indexOf( " by ") + 3)

        var topResult = spotify.requestSearchTrack("$inputsong $inputartist")
        if(topResult!= null) {
            spotify.requestPlayTrack(topResult)
        }else{
            println("\nno matching track found in spotify search")
            spotify.roughGoogle("$inputsong $inputartist")
        }
    }


    fun startSpeechRecognition() {
        //Start a new Thread so our application don't lags

        speechRecThread = Thread {
            //println("\nspeech rec thread open\n")
            try {
                duplex.recognize(mic.targetDataLine, mic.audioFormat)
            } catch (e: LineUnavailableException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        speechRecThread!!.start()
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
}

