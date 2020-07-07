package com.spotifyVoice

import com.darkprograms.speech.recognizer.GSpeechResponseListener
import com.darkprograms.speech.recognizer.GoogleResponse

class SpeechResponseListener(speechRec: SpeechRec) : GSpeechResponseListener {
    val sp = speechRec
    override fun onResponse(gr: GoogleResponse?) {
        var output = gr!!.response
        println(output)
        if (output != null && gr.isFinalResponse) {
            print(output)
            sp.stopSpeechRecognition()
            sp.handleSpeech(gr)
        }
    }
}