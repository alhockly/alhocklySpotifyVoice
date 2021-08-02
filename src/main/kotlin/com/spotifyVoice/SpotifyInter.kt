package authorization.authorization_code.com.spotifyVoice

import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying
import com.wrapper.spotify.model_objects.specification.Track


interface SpotifyInter {



    fun requestPlayUri(uri : String)

    fun getCurrentlyPlaying() : CurrentlyPlaying?

    var lastrequestedTrack : Track?
}