package com.spotifyVoice


import authorization.authorization_code.com.spotifyVoice.GoogleSearch
import com.google.gson.JsonArray
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.SpotifyWebApiException
import com.wrapper.spotify.exceptions.detailed.NotFoundException
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException
import org.apache.hc.core5.http.ParseException
import java.io.*
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
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
    }

    fun requestSearchTrack(searchTerm : String): String? {
        try {
            return searchTrack(searchTerm)

        } catch (e: UnauthorizedException) {
            if (checkAuthStatus()) {
                return searchTrack(searchTerm)
            }
        }

        return null
    }

    fun searchTrack(searchTerm : String): String? {

        var search = spotifyApi.searchTracks(searchTerm).build().execute()
        if (search.items.isNotEmpty()) {
            //look through array and find most accurate match
            println("\nSpotify search results:::::::::::::")
            for (track in search.items) {
                println("${track.artists[0].name} - ${track.name} | ${track.album.name}")
            }
            println(":::::::::::::::::::::::::::::::::::::")
            return search.items[0].uri
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

    //TODO merge results into one list
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

    fun roughGoogle(search: String) {
        var url = GoogleSearch().search(search.trim())
        if(url != null){
            var uri = urlToUri(url)
            if(uri != null){
                requestPlayTrack(uri)
            } else {
                print("failed to find uri for playback")
            }
        } else {
            print("no results found on google")
        }
    }

    fun playUri(uri: String) {
        if (deviceId == null) {
            userSelectPlaybackDevice()
        }
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

                try {
                    deviceId = devices[cleanedInput-1].id
                    inputBad = false
                } catch (e : IndexOutOfBoundsException){
                    print("please enter a number in the list above:      ")
                }

            } catch (e : NumberFormatException){
                print("please enter a number:     ")
            }
        }
        writeAuthCodeToFile(Userprefs(userprefs?.accessToken, userprefs?.refreshToken, userprefs?.authCode,userprefs?.clientID, userprefs?.clientSecret, deviceId))
    }

    fun requestPlayTrack(uri: String) {
        try {
            playUri(uri)
        } catch (e: UnauthorizedException) {
            if (checkAuthStatus()) {
                playUri(uri)
            }
        }
    }

    fun requestPausePlayback(pause: Boolean) {
        try {
            pausePlayback(pause)
        } catch (e: UnauthorizedException) {
            if (checkAuthStatus()) {
                pausePlayback(pause)
            }
        }
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


    enum class UrlType(val value : String){
        TRACK("/track/"),
        ARTIST("/artist/"),
        PLAYLIST("/playlist/"),
        ALBUM("/album/")

    }

    fun urlToUri(url : String) : String?{
        var parts = url.split("/")
        var newuri = parts[parts.size-1]

        if(newuri.contains("?")){
            newuri = newuri.substring(0,newuri.indexOf("?"))
        }
        if(url.contains(UrlType.TRACK.value)){
            return "spotify:track:"+newuri
        }
        if(url.contains(UrlType.ARTIST.value)){
            return "spotify:artist:"+newuri
        }
        if(url.contains(UrlType.PLAYLIST.value)){
            return "spotify:playlist:"+newuri
        }
        if (url.contains(UrlType.ALBUM.value)){
            return "spotify:album:"+newuri
        }
        return null
    }
    //https://open.spotify.com/track/5hqh0JUxRShhqdaxu7wlz5?si=9Z98g3YoRK-ZeG7ujsf4kw
    //spotify:track:5hqh0JUxRShhqdaxu7wlz5

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
            ObjectInputStream(FileInputStream(File("spotify.auth"))).use { it ->
                //Read the family back from the file
                return it.readObject() as Userprefs
            }
        }catch (e : FileNotFoundException){
            return null
        } catch (f : WriteAbortedException){
            return null
        }
    }

    fun writeAuthCodeToFile(userprefs: Userprefs){
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