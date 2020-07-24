import authorization.authorization_code.com.spotifyVoice.mDnsService
import com.spotifyVoice.Main
import org.junit.Test

class mDnsTest {

   var main = Main()

    @Test
    fun mDnsTest(){
        var mdns = mDnsService(main)
        Thread.sleep(200000)
    }
}