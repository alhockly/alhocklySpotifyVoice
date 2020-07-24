import com.spotifyVoice.Spotify
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException
import org.junit.Before
import org.junit.Test

class SpotifyApiTests {
    lateinit var spotify : Spotify

    @Before
    fun setUp(){
        spotify = Spotify()
    }

    @Test
    fun GenericSearch(){
        if (spotify.checkAuthStatus()) {
           search("walk comethazine","track,artist,album")
        }
    }

    @Test
    fun playSpotify(){
        spotify.requestPausePlayback(true)
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