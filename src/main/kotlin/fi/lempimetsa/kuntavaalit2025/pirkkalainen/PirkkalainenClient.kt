package fi.lempimetsa.kuntavaalit2025.pirkkalainen

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson

class PirkkalainenClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    companion object {
        const val BASE_URL = "https://api.vaalikone.online/api/public"
    }

    suspend fun questions(electionId: Int): QuestionsResponse =
        client.get("$BASE_URL/questions?filter[election_id]=$electionId").body()

    suspend fun candidates(electionId: Int): List<Candidate> =
        client.get("$BASE_URL/$electionId/candidates?include=party,answers&sort=candidate_number").body()
}
