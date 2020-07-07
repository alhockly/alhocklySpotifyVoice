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

    lateinit var spotify : Spotify


    fun start(){
        spotify = Spotify()
        //Duplex Configuration
        duplex.language = "en"
        duplex.addResponseListener(SpeechResponseListener(this))
        startSpeechRecognition()
    }


    fun handleSpeech(gr : GoogleResponse){
        val text = gr.response
        if (text.contains("play") && text.contains("by")){
            var inputsong = text.substring(text.indexOf("play") + 4, text.indexOf("by"))
            var inputartist = text.substring(text.indexOf("by") + 2)
            //Online().OpenYoutube(inputsong, inputartist)


            var topResult = spotify.searchTrack(inputsong, inputartist)
            if(topResult!= null) {
                spotify.playTrack(topResult)
            }else{
                print("no matching track found")
                spotify.roughGoogle("$inputsong by$inputartist spotify")
            }

        }
        exit(0)
    }

    fun startSpeechRecognition() {
        //Start a new Thread so our application don't lags
        Thread {
            try {
                duplex.recognize(mic.targetDataLine, mic.audioFormat)
            } catch (e: LineUnavailableException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * Stops the Speech Recognition
     */
    fun stopSpeechRecognition() {
        mic.close()
        //println("\nStopping Speech Recognition...." + " , Microphone State is:" + mic.state)

    }
}