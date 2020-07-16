package authorization.authorization_code.com.spotifyVoice

import com.darkprograms.speech.microphone.Microphone
import com.darkprograms.speech.recognizer.GSpeechDuplex
import java.io.IOException
import javax.sound.sampled.LineUnavailableException

class SpeechRecThread(duplex: GSpeechDuplex, mic : Microphone) : Runnable {
    var duplex=duplex
    var mic=mic
    override fun run() {
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

    fun Thread.UncaughtExceptionHandler(){

    }

}