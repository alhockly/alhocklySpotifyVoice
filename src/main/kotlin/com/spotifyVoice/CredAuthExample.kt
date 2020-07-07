package authorization.client_credentials

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.SpotifyWebApiException
import com.wrapper.spotify.model_objects.credentials.ClientCredentials
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest
import org.apache.hc.core5.http.ParseException

import java.io.IOException
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

object ClientCredentialsExample {
    private val clientId = "5df25986187c4d4fa2e00f6a1285372a"
    private val clientSecret = "9d3d0d78a4b249c180185297830e5224"

    private val spotifyApi = SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .build()
    private val clientCredentialsRequest = spotifyApi.clientCredentials()
        .build()

    fun clientCredentials_Sync() {
        try {
            val clientCredentials = clientCredentialsRequest.execute()

            // Set access token for further "spotifyApi" object usage
            spotifyApi.accessToken = clientCredentials.accessToken

            println("Expires in: " + clientCredentials.expiresIn!!)
        } catch (e: IOException) {
            println("Error: " + e.message)
        } catch (e: SpotifyWebApiException) {
            println("Error: " + e.message)
        } catch (e: ParseException) {
            println("Error: " + e.message)
        }

    }

    fun clientCredentials_Async() {
        try {
            val clientCredentialsFuture = clientCredentialsRequest.executeAsync()

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            val clientCredentials = clientCredentialsFuture.join()

            // Set access token for further "spotifyApi" object usage
            spotifyApi.accessToken = clientCredentials.accessToken

            println("Expires in: " + clientCredentials.expiresIn!!)
        } catch (e: CompletionException) {
            println("Error: " + e.cause!!.message)
        } catch (e: CancellationException) {
            println("Async operation cancelled.")
        }

    }

    @JvmStatic
    fun main(args: Array<String>) {
        clientCredentials_Sync()
        clientCredentials_Async()
    }
}