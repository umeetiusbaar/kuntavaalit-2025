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

    suspend fun candidates(): CandidatesResponsePageProps {
        val response: CandidatesResponse =
            client.get("https://www.vaalikone.fi/_next/data/IYqExYjFyZodhxQG4APcY/kunta2025/al/candidates.json?election=kunta2025&brand=al")
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
        val response: CandidateResponse =
            client.get("https://www.vaalikone.fi/_next/data/IYqExYjFyZodhxQG4APcY/kunta2025/al/candidates/${candidate.id}.json?election=kunta2025&brand=al&id=${candidate.id}")
                .body()
        return response.pageProps
    }
}
