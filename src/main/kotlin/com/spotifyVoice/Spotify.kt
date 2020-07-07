package com.spotifyVoice


import authorization.authorization_code.com.spotifyVoice.GoogleSearch
import authorization.authorization_code.com.spotifyVoice.Online
import com.google.gson.JsonArray
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.SpotifyWebApiException
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException
import com.wrapper.spotify.model_objects.specification.Paging
import com.wrapper.spotify.model_objects.specification.Track
import org.apache.hc.core5.http.ParseException
import java.io.*
import java.lang.Exception
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionException

class Spotify {

    var redirectUrl = "http://localhost/"
    var deviceId : String? = "549fec7e69c54b8e1ff5798313f3be6d10a5bd7a"

    var spotifyApi = SpotifyApi.Builder()
        .setClientId("5df25986187c4d4fa2e00f6a1285372a")
        .setClientSecret("9d3d0d78a4b249c180185297830e5224")
        .setRedirectUri(URI.create(redirectUrl))
        .build()

    var userprefs = readUserPrefsFromFile()

    var clientCredentialsRequest = spotifyApi.clientCredentials().build()

    init {
        if(userprefs==null){
            userprefs=Userprefs(null,null,null,deviceId)
        } else {
            spotifyApi.accessToken = userprefs!!.accessToken
            spotifyApi.refreshToken = userprefs!!.refreshToken
        }
        if(!isAuthorised()){
            authorizationCodeUri_Sync()
        }
    }

    fun searchTrack(trackName : String, artistName: String) : String? {
        try {
            var search = spotifyApi.searchTracks("$trackName  $artistName").build().execute()
            if(search.items.isNotEmpty()){
                //look through array and find most accurate match
                for(track in search.items){
                    println(track.name)
                }
                return search.items[0].uri
            }

        }catch (e : UnauthorizedException){
            if(checkAuthStatus()){
                var search = spotifyApi.searchTracks("$trackName  $artistName").build().execute()
                if(search.items.isNotEmpty()){
                    //look through array and find most accurate match
                    for(track in search.items){
                        println(track.name)
                    }
                    return search.items[0].uri
                }
            }
        }


        return null
    }

    fun roughGoogle(search : String){
        GoogleSearch().search(search.trim())
    }

    fun playTrack(uri : String){
        if(deviceId == null){
            var devices = spotifyApi.usersAvailableDevices.build().execute()
            deviceId = devices[1].id
        }
        var array =  JsonArray()
        array.add(uri)
        spotifyApi.startResumeUsersPlayback().uris(array).device_id(deviceId).build().execute()

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
    val uri = spotifyApi.authorizationCodeUri()
        .scope("user-read-playback-state,user-modify-playback-state,streaming,user-top-read")
        .show_dialog(false)
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

    data class Userprefs(var accessToken : String? , var refreshToken : String?, var authCode : String?, var deviceId : String?) :Serializable{

    }

}