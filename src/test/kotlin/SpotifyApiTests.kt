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

    fun search(term : String, type : String){
        var res = spotify.spotifyApi.searchItem(term, type).build().execute()
        print(res)
    }
}