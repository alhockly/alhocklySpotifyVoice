import authorization.authorization_code.com.spotifyVoice.SpeechRecInteractor
import authorization.authorization_code.com.spotifyVoice.mDnsInteractor
import com.spotifyVoice.Main
import com.spotifyVoice.SpeechRec
import org.junit.Test
import java.net.Inet4Address

class main : SpeechRecInteractor.MainInter {

    override fun localNetworkMap(): Map<String, Inet4Address> {
        TODO("Not yet implemented")
    }

    override fun startFridayRec() {
        TODO("Not yet implemented")
    }


    @Test
    public fun badQuery(){
        var sp = SpeechRec(Main())

        sp.spotifyPlayTrack("play bob and weave by bfd the pac-man")

    }




}