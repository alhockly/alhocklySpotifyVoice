package authorization.authorization_code.com.spotifyVoice

import com.wrapper.spotify.model_objects.specification.Track
import java.util.*

class Util {
    companion object {
        fun calculateLeven(x: String, y: String): Int {

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

        fun trackNameWithNoArtists(track: Track): String {
            var title = track.name.toLowerCase()
            track.artists.forEach {
                title = title.replace(it.name.toLowerCase(),"")
            }
            title = title.replace("feat.", "").replace("ft.", "")
            return title
        }

        fun removeFeaturesFromTrackTitle(trackName : String) : String{
            var res = trackName
            var feats = listOf("(ft","(feat", "(with", "ft", "feat")
            feats.forEach {
                if(res.contains(it)){
                    res = res.substring(0, res.indexOf(it))
                }
            }

            return res.trim()
        }
    }
}