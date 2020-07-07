package authorization.authorization_code

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.SpotifyHttpManager
import com.wrapper.spotify.exceptions.SpotifyWebApiException
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest
import org.apache.hc.core5.http.ParseException

import java.io.IOException
import java.net.URI
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

object AuthorizationCodeExample {
    private val clientId = "5df25986187c4d4fa2e00f6a1285372a"
    private val clientSecret = "9d3d0d78a4b249c180185297830e5224"
    private val redirectUri = SpotifyHttpManager.makeUri("http://localhost/")
    private val code = "AQDcBYX-edd59vdIH1Du4ZQj6MhN24T3mjp1cwq2HVd3KLWfG_BrBOjjw3peHIveT6_p1chvcZlV0l5qymPbzBKMiqEl1iWSZ5VXZH_I4XlDGQMmJVGDOurxvL1g6ML20QI4mNJAkrR61QDvIQEBQN3GfMlqsCbSl0SjF0Wj5tDzAANgf6FKH8FpJTgm7iuLUZKRV_YHqStv3mfW7zOmeqsq4HnmVo7P1xbs"

    private val spotifyApi = SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRedirectUri(redirectUri)
        .build()
    private val authorizationCodeRequest = spotifyApi.authorizationCode(code)
        .build()

    fun authorizationCode_Sync() {
        try {
            val authorizationCodeCredentials = authorizationCodeRequest.execute()

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.accessToken = authorizationCodeCredentials.accessToken
            spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken

            println("Expires in: " + authorizationCodeCredentials.expiresIn!!)
        } catch (e: IOException) {
            println("Error: " + e.message)
        } catch (e: SpotifyWebApiException) {
            println("Error: " + e.message)
        } catch (e: ParseException) {
            println("Error: " + e.message)
        }

    }

    fun authorizationCode_Async() {
        try {
            val authorizationCodeCredentialsFuture = authorizationCodeRequest.executeAsync()

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            val authorizationCodeCredentials = authorizationCodeCredentialsFuture.join()

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.accessToken = authorizationCodeCredentials.accessToken
            spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken

            println("Expires in: " + authorizationCodeCredentials.expiresIn!!)
        } catch (e: CompletionException) {
            println("Error: " + e.cause!!.message)
        } catch (e: CancellationException) {
            println("Async operation cancelled.")
        }

    }

    @JvmStatic
    fun main(args: Array<String>) {
        authorizationCode_Sync()
        //authorizationCode_Async()
    }
}