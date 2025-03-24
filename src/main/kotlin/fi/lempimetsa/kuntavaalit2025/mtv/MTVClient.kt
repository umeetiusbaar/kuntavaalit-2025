package fi.lempimetsa.kuntavaalit2025.mtv

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson

class MTVClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    companion object {
        const val BASE_URL = "https://mtv-vaalikone.ehdokaskone.fi/api/v1/public/elections"
    }

    suspend fun district(electionId: Int, districtId: Int): DistrictResponse =
        client.get("$BASE_URL/$electionId/districts/$districtId").body()

    suspend fun candidates(electionId: Int, districtId: Int): List<Candidate> =
        client.get("$BASE_URL/$electionId/districts/$districtId/candidates").body()

    suspend fun candidate(electionId: Int, districtId: Int, candidateId: Int): CandidateResponse =
        client.get("$BASE_URL/$electionId/districts/$districtId/candidates/$candidateId").body()
}
