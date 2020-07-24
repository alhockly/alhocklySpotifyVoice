package authorization.authorization_code.com.spotifyVoice

import com.google.gson.Gson
import java.net.URLEncoder
import Json4Kotlin_Base
import java.util.*

class GoogleSearch {
   var  apiKey = "AIzaSyBnOBjQOif7CRdptFRR_m--s7ubGfB8At8"
    var searchEngineKey = "008627090675627990467:hcutbn_2ktw"
    fun search(term :String) : String?{

        var json = Online().wget("https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$searchEngineKey&q=${URLEncoder.encode(term)}")

        val results = Gson().fromJson(json, GsonGoogleSearch::class.java)
        var levenlist = arrayListOf<Pair<Items,Int>>()

        if(results != null && results.items != null) {
            for (res in results.items) {
                var title = res.title.toLowerCase().replace(" on spotify", "").replace(" | spotify playlist", "")
                title = title.replace(" - playlist", "").replace(" | spotify", "").replace(" - a song by","")
                title = title.replace("...","").replace(" - song by","").replace(" - single by","")
                res.title = title
                levenlist.add(Pair(res, calculateLeven(title.trim(), term)))
            }
            println("\n google results :::::::::::::")
            for (item in levenlist) {

                println("${item.first.title} - L(${item.second})")

            }
            println("::::::::::::::::::::::::::::")

            return levenlist[0].first.link
        }
        return null
    }



    internal fun calculateLeven(x: String, y: String): Int {

        val dp = Array(x.length + 1) { IntArray(y.length + 1) }

        for (i in 0..x.length) {
            for (j in 0..y.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = min(
                        dp[i - 1][j - 1] + costOfSubstitution(x[i - 1], y[j - 1]),
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                    )
                }
            }
        }

        return dp[x.length][y.length]
    }

    fun costOfSubstitution(a: Char, b: Char): Int {
        return if (a == b) 0 else 1
    }
    fun min(vararg numbers: Int): Int {
        return Arrays.stream(numbers)
            .min().orElse(Integer.MAX_VALUE)
    }

}
