package authorization.authorization_code.com.spotifyVoice

import authorization.authorization_code.com.spotifyVoice.Util.Companion.calculateLeven
import com.google.gson.Gson
import java.net.URLEncoder
import kotlin.collections.HashMap

class GoogleSearch {
   var  apiKey = "AIzaSyAGrye4zZST9F4cthzM_Sdw9qEsRppBqII"

    fun search(term : String, searchEngineKey : String) : HashMap<String,List<String>>?{

        var json = Online().wget("https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$searchEngineKey&q=${URLEncoder.encode(term)}")

        val results = Gson().fromJson(json, GsonGoogleSearch::class.java)
        var levenlist = arrayListOf<Pair<Items,Int>>()

        if(results != null && results.items != null) {  //Intellij thinks results.items can't be null but it can
            for (res in results.items) {

                var title = res.title.toLowerCase().replace(" on spotify", "").replace(" | spotify playlist", "")
                title = title.replace(" - playlist", "").replace(" | spotify", "").replace(" - a song by", "")
                title = title.replace("...", "").replace(" - song by", "").replace(" - single by", "")

                try{
                    title = ""
                    if(res.link.contains("spotify.com/track")) {
                        var pos = res.snippet.indexOf("on Spotify")
                        title = res.snippet.substring(0, pos).replace("Listen to ", "").trim()
                        title = Util.removeFeaturesFromTrackTitle(title)
                    }
                    if(res.link.contains("youtube")){
                        title = res.title.toLowerCase()
                        title = title.replace("Mix - ", "")
                        if(title.contains("-")){
                            title = title.substring(res.title.indexOf("-")).trim()
                        }
                        title = Util.removeFeaturesFromTrackTitle(title)
                        title = title.replace("official", "").replace("music", "").replace("video", "")
                        title = title.replace("youtube", "")
                        title = title.replace("|", "")
                        title = title.replace("[", "").replace("]", "")
                        title = title.replace("(", "").replace(")", "")
                        title = title.replace(" audio", "").replace("lyrics", "")
                        title = title.replace("...", "")
                        title = title.replace("-","")

                    }

                } catch (e : StringIndexOutOfBoundsException){}
                res.title = title.trim()

                levenlist.add(Pair(res, calculateLeven(title.trim(), term)))
            }

            var suggestedArists = results.items.filter{it.link.contains("spotify.com/artist") && it.title.isNotEmpty()}.map { items -> items.title }
            var suggestedTracks = results.items.filter{it.link.contains("spotify.com/track") or it.link.contains("youtube") && it.title.isNotEmpty()}.map { it.title }
            var suggestedAlbums = results.items.filter{item -> item.link.contains("spotify.com/album") && item.title.isNotEmpty()}.map { items -> items.title }
            var suggestedplaylists = results.items.filter{item -> item.link.contains("spotify.com/playlists") && item.title.isNotEmpty()}.map { items -> items.title }


            println("'$term' google results :::::::::::::")
            suggestedTracks.forEach { println(it + " | Track")}
            suggestedArists.forEach { println(it + " | Artist")}
            suggestedAlbums.forEach { println(it + " | Album")}
            suggestedplaylists.forEach { println(it + " | Playlist")}
            println("::::::::::::::::::::::::::::::::::::::::::::")

            var googleData = HashMap<String, List<String>>()
            if(suggestedTracks.isNotEmpty()){
                googleData.put("track", suggestedTracks)
            }
            if(suggestedArists.isNotEmpty()){
                googleData.put("artist", suggestedArists)
            }
//            if(suggestedAlbums.isNotEmpty()){
//                googleData.put("album", suggestedAlbums)
//            }
//            if(suggestedplaylists.isNotEmpty()){
//                googleData.put("playlist", suggestedplaylists)
//            }

            return googleData
        }

        return null
    }



}
