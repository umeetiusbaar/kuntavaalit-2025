package fi.lempimetsa.kuntavaalit2025.aamulehti

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson

class AamulehtiClient {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    var buildId: String? = null

    suspend fun candidates(): CandidatesResponsePageProps {
        if (buildId == null) {
            initializeBuildId()
        }
        val response: CandidatesResponse =
            client.get("https://www.vaalikone.fi/_next/data/$buildId/kunta2025/al/candidates.json?election=kunta2025&brand=al")
                .body()
        return response.pageProps
    }

    suspend fun candidateSearch(municipality: Municipality): List<Candidate> {
        val response: CandidateSearchResponse =
            client.get("https://vaalikone-api.datadesk.hs.fi/candidateSearch?electionId=kunta2025&size=1000&municipality=${municipality.code}")
                .body()
        return response.item.hits
    }

    suspend fun candidateQuestions(candidate: Candidate): CandidateResponsePageProps {
        if (buildId == null) {
            initializeBuildId()
        }
        val response: CandidateResponse =
            client.get("https://www.vaalikone.fi/_next/data/$buildId/kunta2025/al/candidates/${candidate.id}.json?election=kunta2025&brand=al&id=${candidate.id}")
                .body()
        return response.pageProps
    }

    private suspend fun initializeBuildId() {
        val response = client.get("https://www.vaalikone.fi/kunta2025/al").body<String>()
        val regex = """"buildId":\s*"([^"]+)"""".toRegex()
        val matchResult = regex.find(response) ?: throw RuntimeException("Build ID not found")
        this.buildId = matchResult.groupValues[1]
    }
}
