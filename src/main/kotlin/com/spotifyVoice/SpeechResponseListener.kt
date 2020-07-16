package com.spotifyVoice

import com.darkprograms.speech.recognizer.GSpeechResponseListener
import com.darkprograms.speech.recognizer.GoogleResponse

class SpeechResponseListener(speechRecTimeout: SpeechRec.speechRecTimeout, speechRec: SpeechRec) : GSpeechResponseListener {
    val sp = speechRec
    var ignore = true
    var RecogTimeout = speechRecTimeout

    override fun onResponse(gr: GoogleResponse?) {
        if(!ignore) {
            RecogTimeout.cancel = true
            var output = gr!!.response
            println(output)
            if (output != null && gr.isFinalResponse) {
                println(output)
                sp.stopSpeechRecognition()
                sp.handleSpeech(gr.response.toLowerCase())
                return
            }
        }
    }
}