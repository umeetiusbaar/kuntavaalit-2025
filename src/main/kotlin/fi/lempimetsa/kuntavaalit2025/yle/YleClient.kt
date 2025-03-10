package fi.lempimetsa.kuntavaalit2025.yle

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson

class YleClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    companion object {
        const val BASE_URL = "https://vaalit.yle.fi/vaalikone/alue-ja-kuntavaalit2025/api/public"
    }

    suspend fun constituencies(): List<Constituency> = client.get("$BASE_URL/municipality/constituencies").body()

    suspend fun candidates(constituencyId: Int): List<Candidate> =
        client.get("$BASE_URL/municipality/constituencies/$constituencyId/candidates").body()

    suspend fun parties(constituencyId: Int): List<Party> =
        client.get("$BASE_URL/municipality/constituencies/$constituencyId/parties").body()

    suspend fun questions(constituencyId: Int): List<Category> =
        client.get("$BASE_URL/municipality/constituencies/$constituencyId/questions").body()

    suspend fun candidate(constituencyId: Int, candidateId: Int): Candidate =
        client.get("$BASE_URL/municipality/constituencies/$constituencyId/candidates/$candidateId").body()
}
