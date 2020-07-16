package authorization.authorization_code.com.spotifyVoice

import java.net.Inet4Address

interface SpeechRecInteractor {


    interface SpeechInter{

    }

    interface MainInter{
        fun localNetworkMap() : Map<String,Inet4Address>
    }
}