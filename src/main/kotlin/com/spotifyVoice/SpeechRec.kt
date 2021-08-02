package com.spotifyVoice

import authorization.authorization_code.com.spotifyVoice.Online
import authorization.authorization_code.com.spotifyVoice.SpeechRecInteractor
import authorization.authorization_code.com.spotifyVoice.Util
import com.goxr3plus.speech.microphone.Microphone
import com.goxr3plus.speech.recognizer.GSpeechDuplex
import com.wrapper.spotify.model_objects.specification.Track
//import com.darkprograms.speech.microphone.Microphone
//import com.darkprograms.speech.recognizer.GSpeechDuplex
import net.sourceforge.javaflacencoder.FLACFileWriter
import java.io.IOException
import java.lang.IllegalArgumentException
import javax.sound.sampled.LineUnavailableException
import kotlin.system.exitProcess


class SpeechRec(mainClassInterator : SpeechRecInteractor.MainInter) : SpeechRecInteractor.SpeechInter {
    private  var  mic : Microphone
    var main = mainClassInterator
    val duplex = GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw", main)


    var spotify = Spotify()
    var speechHandled = false
    var speechRecThread : Thread? = null
    var speechRecTimeoutThread = speechRecTimeout(duplex,this, mainClassInterator)
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

        //TODO load google api key here
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
               spotify.requestApiFunction {  spotify.playUri(search)}
            }
            speechHandled = true
            return
        }

        if(text.contains("play spotify") || text.contains("pause spotify")){
            spotify.requestApiFunction { spotify.pausePlayback((text.contains("play")))}
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
        var inputsong = text.substring(text.indexOf("play ") + 5, text.indexOf(" by ")).trim()
        var inputartist = text.substring(text.indexOf( " by ") + 3).trim()

        //Get some extra info on what the user means via google with the added context of spotify
        var googleData = spotify.requestGoogle(inputsong, inputartist)

        if(googleData!= null && googleData.isNotEmpty()) {
            var artistmap = hashMapOf<String, Int>()
            var trackmap = hashMapOf<String, Int>()

            googleData.get("artist")?.forEach{
                artistmap.put(it,Util.calculateLeven(it, inputartist))
            }

            if(artistmap.isNotEmpty()){
                var topartists = artistmap.toList().sortedBy { (_, value ) -> value }
                if (topartists[0].second < 8){
                    inputartist = topartists[0].first
                    print("assuming proper artist name is  \" $inputartist \" ")
                }
            }

            googleData.get("track")?.forEach{
                //may not be nessesary
                var trackWithoutArtist = it.toLowerCase().replace(inputartist.toLowerCase(),"").trim()
                //TODO phonetic version of levenstien
                trackmap.put(trackWithoutArtist, Util.calculateLeven(trackWithoutArtist, inputsong.toLowerCase()))
            }

            if(trackmap.isNotEmpty()){
                var toptracks = trackmap.toList().sortedBy { (_, value ) -> value }
                if (toptracks[0].second < 8){
                    inputsong = toptracks[0].first
                    print("assuming proper track name is \" $inputsong \" ")
                }
            }
        }

        var tracks = spotify.requestSearchTrack("$inputsong $inputartist")
        if (tracks != null){

            var finalResultMap = HashMap<Track, Int>()
            tracks.forEach {
                finalResultMap.put(it, Util.calculateLeven(inputsong.toLowerCase(), Util.trackNameWithNoArtists(it)))
            }
            var finalResults = finalResultMap.toList().sortedBy { (_, value ) -> value }
            if(finalResults.isNotEmpty()) {

                if(finalResults.stream().filter{it.second == finalResults[0].second}.count() > 1){  //is there multiple tracks with the best leven?
                    //this is to resolve when there are multiple of the same score, prefer explicit and non-
                    var prioritisedResultMap = HashMap<Track, Int>()
                    finalResultMap.forEach{
                        var newVal = it.value
                        if(it.key.isExplicit){
                           newVal = newVal -1
                        }
                        if(it.key.album.albumType.toString() != "COMPILATION"){
                            newVal = newVal -1
                        }
                        prioritisedResultMap.put(it.key, newVal)
                    }
                    finalResults = prioritisedResultMap.toList().sortedBy { (_, value ) -> value }
                }

                var topResult = finalResults[0]

                if (topResult.second < 40) {    //sanity check
                    spotify.requestApiFunction { spotify.playUri(topResult.first.uri) }
                    var queue =  spotify.requestRecommendations(topResult.first.id)
                    if(queue!=null) {
                        spotify.requestApiFunction { spotify.addUrisToQueue(queue) }
                    }
                    return
                }
                print("No closely matching track found")
            }
            println("no results for $inputartist - $inputsong")
        } else {
            println("no results for $inputartist - $inputsong")
        }
    }


    fun startSpeechRecognition() {
        duplex.stopSpeechRecognition()
        val threadSet = Thread.getAllStackTraces()
        for( thread in Thread.getAllStackTraces().keys){
            if(thread.name.equals("Downstream Thread")){
               // thread.stop()
                ///find better way to end old threads
                //thread.setUncaughtExceptionHandler(Ex)
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
        //Measures time between starting Google speech rec and getting a response back
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

