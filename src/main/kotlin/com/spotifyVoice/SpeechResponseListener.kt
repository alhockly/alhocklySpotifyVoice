package com.spotifyVoice

import com.darkprograms.speech.recognizer.GSpeechResponseListener
import com.darkprograms.speech.recognizer.GoogleResponse

class SpeechResponseListener(speechRec: SpeechRec) : GSpeechResponseListener {
    val sp = speechRec
    var ignore = true

    override fun onResponse(gr: GoogleResponse?) {
        if(!ignore) {
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