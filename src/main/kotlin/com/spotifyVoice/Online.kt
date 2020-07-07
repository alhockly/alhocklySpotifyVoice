package authorization.authorization_code.com.spotifyVoice

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

class Online {


    @Throws(IOException::class, URISyntaxException::class)
    fun OpenYoutube(trackname: String, artist: String) {
        println("opening $trackname - $artist")
        val html = wget("https://www.youtube.com/results?search_query=" + trackname.trim { it <= ' ' }.replace(
            " ",
            "+"
        ) + "+" + artist.trim { it <= ' ' }.replace(" ", "+")
        )
        val linkstart = html.indexOf("watch?v=")
        //System.out.println(linkstart);
        val link = "https://www.youtube.com/" + html.substring(linkstart, linkstart + 19)

        //System.out.println(link);
        java.awt.Desktop.getDesktop().browse(URI(link))
    }

    @Throws(IOException::class)
    fun wget(url: String): String {
        val myURL = URL(url)
        System.setProperty("http.agent", "")
        //System.out.println(url);
        val connection = myURL.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.doOutput = true
        connection.connect()
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val results = StringBuilder()
//        var line = reader.readLine()
//        while ((line = reader.readLine()) != null) {
//            results.append(line)
//        }
        reader.forEachLine { results.append(it) }
        connection.disconnect()
        return results.toString()
    }

    companion object {


        @JvmStatic
        fun main(args: Array<String>) {
            //test/usage example
            try {
                Online().OpenYoutube("a lot", "21 savage")
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

        }
    }
}
