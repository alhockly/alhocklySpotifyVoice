import com.spotifyVoice.Spotify
import org.junit.Test

class TestGenericSpotifySearch {
    companion object{
        @JvmStatic
        fun main(args : List<String>){

        }
    }

    @Test
    fun makeSearch(){
        var spotify = Spotify()
        var res = spotify.spotifyApi.searchItem("walk comethazine","album,artist,track").build().execute()





        print(res)
    }

}