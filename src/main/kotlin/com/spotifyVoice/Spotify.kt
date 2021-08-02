package com.spotifyVoice

import authorization.authorization_code.com.spotifyVoice.GoogleSearch
import com.google.gson.JsonArray
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.SpotifyWebApiException
import com.wrapper.spotify.exceptions.detailed.NotFoundException
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException
import com.wrapper.spotify.model_objects.specification.Track
import org.apache.hc.core5.http.ParseException
import java.io.*
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import java.lang.NumberFormatException
import java.net.URI
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionException

class Spotify {
    var userprefs = readUserPrefsFromFile()

    var redirectUrl = "http://localhost/"
    var deviceId: String? = null
    var spotifyApi = SpotifyApi.Builder()
        .setRedirectUri(URI.create(redirectUrl))
        .build()


    init {
        if (userprefs == null) {
            userprefs = Userprefs(null, null, null,null,null, null)
        } else {
            spotifyApi = SpotifyApi.Builder()
                .setClientId(userprefs!!.clientID)
                .setClientSecret(userprefs!!.clientSecret)
                .setRedirectUri(URI.create(redirectUrl))
                .build()
            spotifyApi.accessToken = userprefs!!.accessToken
            spotifyApi.refreshToken = userprefs!!.refreshToken
            deviceId = userprefs!!.deviceId
        }
        if (!isAuthorised()) {
            authorizationCodeUri_Sync()
        }
        //trackwatcher = currentSongWatcher(this)
        //trackwatcher.run()
    }

    fun requestSearchTrack(searchTerm : String): Array<Track>? {
        try {
            return searchTrack(searchTerm)
        } catch (e: UnauthorizedException) {
            if (checkAuthStatus()) {
                return searchTrack(searchTerm)
            }
        } catch (e : NoClassDefFoundError){
            println("Exception: Could not complete search")
        }
        return null
    }

    fun searchTrack(searchTerm : String): Array<Track>? {
        var search = spotifyApi.searchTracks(searchTerm).build().execute()
        if (search.items.isNotEmpty()) {
            //look through array and find most accurate match
            println("\nSpotify search results:::::::::::::")
            for (track in search.items) {
                println("${track.artists[0].name} - ${track.name} | ${track.album.name}")
            }
            println(":::::::::::::::::::::::::::::::::::::")
            return search.items
        }
        return null
    }

    fun requestGenericSearch(types : String, search : String) : String?{
        try {
            return GenericSearch(search, types)
        } catch (e: UnauthorizedException) {
            if (checkAuthStatus()) {
                return GenericSearch(search, types)
            }
        }
        println("no found matches")
        return null
    }

    fun GenericSearch(types: String, search: String) : String?{
        var results = spotifyApi.searchItem(types,search).build().execute()
        if(results != null){
            if(results.artists.items.isNotEmpty()) {
                return results.artists.items[0].uri
            }
        }
        else{
            println("no results for generic search")
        }
        return null
    }

    fun requestGoogle(inputtrack :String, inputArtist : String) : HashMap<String, List<String>>? {
        var spotifysearchEngineKey = "525538dae59e71f3a"
        var yotubesearchEngineKey = "7248d67f8cfc164a3"

        try {
            println("google + spotify")
            var googleData = GoogleSearch().search("$inputArtist - $inputtrack", spotifysearchEngineKey)
            if (googleData == null || googleData.isEmpty()) {
                println("google + youtube")
                googleData = GoogleSearch().search("$inputArtist - $inputtrack", yotubesearchEngineKey)
            } else{
                if(googleData.size <5){ // or levendists are bad?
                    println("google + youtube")
                    var youtubeGoogleData = GoogleSearch().search("$inputArtist - $inputtrack", yotubesearchEngineKey)
                    if(youtubeGoogleData != null) {
                        youtubeGoogleData.forEach{
                            if(googleData.containsKey(it.key)){
                                var list = googleData.get(it.key)!!.toMutableList()
                                it.value.forEach { list.add(it) }
                                googleData.replace(it.key, list)
                            } else{
                                googleData.put(it.key, it.value)
                            }
                        }
                    }
                }
            }
            return googleData
        }catch (e: NullPointerException){
            e.printStackTrace()
        } catch (e : Exception){
            print("google request failed cuz of $e")
        }
        return null
    }

    fun playUri(uri: String) {
        if (deviceId == null) { userSelectPlaybackDevice() }
        try {
            if (uri.contains("track")) {
                var array = JsonArray()
                array.add(uri)
                spotifyApi.startResumeUsersPlayback().uris(array).device_id(deviceId).build().execute()
            }
            if (uri.contains("artist") || uri.contains("album") || uri.contains("playlist")) {
                spotifyApi.startResumeUsersPlayback().context_uri(uri).device_id(deviceId).build().execute()
            }
        } catch (e : NotFoundException){
            userSelectPlaybackDevice()
            playUri(uri)
        } catch (e : Exception){
            e.printStackTrace()
        }
        println("playing $uri")
    }

    fun userSelectPlaybackDevice(){
        var devices = spotifyApi.usersAvailableDevices.build().execute()
        var count = 1
        for (d in devices){
            println("$count. ${d.name} - ${d.type}")
            count++
        }
        print("please enter the number of the device you wish to use:     ")
        var inputBad = true
        var input : String?
        var cleanedInput : Int
        while(inputBad) {
            input = readLine()
            try{
                cleanedInput = input!!.toInt()
                deviceId = devices[cleanedInput-1].id
                break

            } catch (e : NumberFormatException){
                print("please enter a number:     ")
            } catch (e : IndexOutOfBoundsException){
                print("please enter a number in the list above:      ")
            }
        }
        writeAuthCodeToFile(Userprefs(userprefs?.accessToken, userprefs?.refreshToken, userprefs?.authCode,userprefs?.clientID, userprefs?.clientSecret, deviceId))
    }



    fun requestRecommendations(itemID : String) : List<String>?{
        try {
            return getReccomendations(itemID)
        } catch (e: UnauthorizedException) {
            if (checkAuthStatus()) {
                return getReccomendations(itemID)
            }
        }
        return null
    }

    fun requestApiFunction(function : () -> Unit){
        //USE this to safely call api functions that dont return anything
        try {
            return function.invoke()
        } catch (e: UnauthorizedException) {
            if (checkAuthStatus()) {
               return function.invoke()
            }
        }
    }

    fun addUrisToQueue(trackUris : List<String>){
        trackUris.forEach {
            spotifyApi.addItemToUsersPlaybackQueue(it).build().execute()
        }
        print("added ${trackUris.size} tracks to queue")
    }

    fun getReccomendations(itemID : String) : List<String>? {
        var data =spotifyApi.recommendations.seed_tracks(itemID).build().execute()
        if(data!= null && data.tracks.isNotEmpty()){
            var list = data.tracks.map { it.uri }
            print("")
            return list
        }
        return null
    }

    fun pausePlayback(pause : Boolean){
        try {
            if (!pause) {
                spotifyApi.pauseUsersPlayback().build().execute()
            } else {
                spotifyApi.startResumeUsersPlayback().device_id(deviceId).build().execute()
            }
        }catch (e : Exception){
            //e.printStackTrace()
            print("exception for playing when u should pause or vice versa")
        }
    }


    fun isAuthorised() : Boolean {
        if(userprefs!=null){
            if(userprefs!!.accessToken == null){
                return false
            } else {
                spotifyApi.refreshToken=userprefs!!.refreshToken
                spotifyApi.accessToken=userprefs!!.accessToken
//                if(checkAuthStatus()){
//                    return true
//                }
                if(spotifyApi.refreshToken != null){
                    return true
                }
            }
        } else{
            return false
        }
        return false
    }

    fun readUserPrefsFromFile() : Userprefs?{
        try {
            ObjectInputStream(FileInputStream(File("spotify.auth"))).use {
                //Read the prefs back from the file
                return it.readObject() as Userprefs
            }
        }catch (e : FileNotFoundException){
            return null
        } catch (f : WriteAbortedException){
            return null
        }
    }

    fun writeAuthCodeToFile(userprefs: Userprefs){
        //TODO check file exists and create if not
        if(File("spotify.auth").exists()){

        }
        try {
            ObjectOutputStream(FileOutputStream(File("spotify.auth"))).use { it -> it.writeObject(userprefs) }
        } catch (e : Exception){
            e.printStackTrace()
            println("error writing auth to file")
        }
    }

    fun checkAuthStatus() : Boolean{
        if(userprefs!!.refreshToken == null){
            return false
        }
        var res = spotifyApi.authorizationCodeRefresh().refresh_token(spotifyApi.refreshToken).build().execute()
        if(res.accessToken != null){
            spotifyApi.accessToken=res.accessToken
        }
        return true
    }

    fun authorizationCodeUri_Sync() {
        if(userprefs!!.clientID == null){
            print("enter clientID: ")
            userprefs!!.clientID = readLine()
        }
        if(userprefs!!.clientSecret == null){
            print("enter clientSecret:  ")
            userprefs!!.clientSecret = readLine()
        }
        spotifyApi = SpotifyApi.Builder()
            .setClientId(userprefs!!.clientID)
            .setClientSecret(userprefs!!.clientSecret)
            .setRedirectUri(URI.create(redirectUrl))
            .build()

        val uri = spotifyApi.authorizationCodeUri()
            .scope("user-read-playback-state,user-modify-playback-state,streaming,user-top-read")
            .show_dialog(true)
            .build().execute()

        println("URI: $uri")
        print("Enter code: ")
        authorizationCode_Sync(readLine()!!.replace(redirectUrl,""))
    }

    fun authorizationCode_Sync(code : String) : Boolean{
        try {

            val authorizationCodeRequest = spotifyApi.authorizationCode(code).build()
            val authorizationCodeCredentials = authorizationCodeRequest.execute()

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.accessToken = authorizationCodeCredentials.accessToken
            spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken

            println("Expires in: " + authorizationCodeCredentials.expiresIn!!)

            userprefs!!.accessToken = spotifyApi.accessToken
            userprefs!!.refreshToken = spotifyApi.refreshToken
            userprefs!!.authCode=code
            writeAuthCodeToFile(userprefs!!)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            println("Error: " + e.message)
            return false
        } catch (e: SpotifyWebApiException) {
            println("Error: " + e.message)
            return false
        } catch (e: ParseException) {
            println("Error: " + e.message)
            return false
        }

    }


    fun authorizationCodeUri_Async() {
        try {
            val uriFuture = spotifyApi.authorizationCodeUri()
                .scope("user-modify-playback-state,streaming,user-top-read")
                .show_dialog(false)
                .build().executeAsync()

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            val uri = uriFuture.join()

            println("URI: $uri")
        } catch (e: CompletionException) {
            println("Error: " + e.cause!!.message)
        } catch (e: CancellationException) {
            println("Async operation cancelled.")
        }

    }



    data class Userprefs(var accessToken : String? , var refreshToken : String?, var authCode : String?, var clientID : String?, var clientSecret : String? , var deviceId : String?) :Serializable
}