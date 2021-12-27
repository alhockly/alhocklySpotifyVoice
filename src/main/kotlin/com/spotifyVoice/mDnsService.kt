//package authorization.authorization_code.com.spotifyVoice
//
//import java.io.IOException
//import java.net.InetAddress
//import java.net.UnknownHostException
//import javax.jmdns.JmDNS
//import javax.jmdns.ServiceEvent
//import javax.jmdns.ServiceListener
//
//class mDnsService(mainInteractor : mDnsInteractor.MainInter) : ServiceListener, mDnsInteractor{
//
//    var main = mainInteractor
//
//    var jmDns : JmDNS = JmDNS.create(InetAddress.getLocalHost())
//
//    init {
//        try {
//            // Add a service listener
//            jmDns.addServiceListener("_http._tcp.local.", this)
//        } catch (e: UnknownHostException) {
//            println(e.message)
//        } catch (e: IOException) {
//            println(e.message)
//        }
//    }
//
//
//    override fun serviceRemoved(event: ServiceEvent?) {
//    }
//
//    override fun serviceAdded(event: ServiceEvent?) {
//    }
//
//    override fun serviceResolved(event: ServiceEvent?) {
//        if(event != null) {
//            main.addAddressToLocalMap(Pair(event.name, event.info.inet4Addresses[0]))
//        }
//    }
//
//
//}