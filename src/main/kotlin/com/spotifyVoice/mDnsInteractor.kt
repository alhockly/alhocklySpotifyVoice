package authorization.authorization_code.com.spotifyVoice

import java.net.Inet4Address

interface mDnsInteractor {


    interface MainInter{
        fun addAddressToLocalMap(address : Pair<String, Inet4Address>)
        fun removeAddressFromLocalMap(inet: Inet4Address)
    }


}