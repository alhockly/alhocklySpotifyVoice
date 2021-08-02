import com.spotifyVoice.Spotify
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

class SpotifyApiTests {
    var spotify = Spotify()



    @Test
    fun GenericSearch(){
        spotify = Spotify()
        if (spotify.checkAuthStatus()) {
           search("walk comethazine","track,artist,album")
        }
    }

    @Test
    fun playSpotify(){
       spotify.requestApiFunction { spotify.pausePlayback(true) }
    }

    fun search(term : String, type : String){
        var res = spotify.spotifyApi.searchItem(term, type).build().execute()
        print(res)
    }


    @Test
    fun deviceSelect(){
        spotify.deviceId = null
        if (spotify.checkAuthStatus()) {
            spotify.userSelectPlaybackDevice()
        }

    }
}