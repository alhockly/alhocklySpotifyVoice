package com.spotifyVoice

import authorization.authorization_code.com.spotifyVoice.SpeechRecInteractor
import com.darkprograms.speech.recognizer.GSpeechResponseListener
import com.darkprograms.speech.recognizer.GoogleResponse

class SpeechResponseListener(speechRecTimeout: SpeechRec.speechRecTimeout, speechRec: SpeechRec, mainClass: SpeechRecInteractor.MainInter) : GSpeechResponseListener {
    val sp = speechRec
    var ignore = true
    var RecogTimeout = speechRecTimeout
    var main = mainClass

    override fun onResponse(gr: GoogleResponse?) {
        if(!ignore) {
            RecogTimeout.cancel = true
            var output = gr!!.response
            println(output)
            if (output != null && gr.isFinalResponse) {
                println(output)
                sp.stopSpeechRecognition()
                try {
                    sp.handleSpeech(gr.response.toLowerCase())
                } catch (e : Exception){
                    e.printStackTrace()
                }
                main.startFridayRec()
                //TODO remove loop by using this
                return
            }
        }
    }
}