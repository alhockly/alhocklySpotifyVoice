import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Online {


    public void OpenYoutube(String trackname, String artist) throws IOException, URISyntaxException {
        System.out.println("opening "+trackname+" - "+artist);
        String html = wget("https://www.youtube.com/results?search_query="+trackname.trim().replace(" ","+")+"+"+artist.trim().replace(" ","+"));
        int linkstart=html.indexOf("watch?v=");
        //System.out.println(linkstart);
        String link = "https://www.youtube.com/"+html.substring(linkstart,linkstart+19);

        //System.out.println(link);
        java.awt.Desktop.getDesktop().browse(new URI(link));
    }

    public  String wget(String url) throws IOException {
        URL myURL = new URL(url);
        System.setProperty("http.agent", "");
        //System.out.println(url);
        HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder results = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            results.append(line);
        }
        connection.disconnect();
        return results.toString();
    }
}
