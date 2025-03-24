package fi.lempimetsa.kuntavaalit2025.aamulehti

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class AamulehtiClientITest {

    private val client = AamulehtiClient()

    @Test
    fun candidates() = runBlocking {
        val candidates = client.candidates()
        val parties = candidates.parties
        parties.forEach { party ->
            println("${party.id}\t${party.shortName}")
        }
    }

    @Test
    fun `print all parties in municipalities`() = runBlocking {
        val parties = client.candidates().parties.associateBy { it.id }
        Municipality.entries.forEach { municipality ->
            val candidates = client.candidateSearch(municipality)
            candidates.mapNotNull { candidate -> parties[candidate.party] }.distinct().forEach { party ->
                println("${party.id}\t${municipality.name}\t${party.shortName}")
            }
            println()
        }
    }
}