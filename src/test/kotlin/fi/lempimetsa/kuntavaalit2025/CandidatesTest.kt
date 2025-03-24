package fi.lempimetsa.kuntavaalit2025

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CandidatesTest {

    @Nested
    inner class PutIfAbsent {

        @BeforeEach
        fun setUp() {
            Candidates.clear()
        }

        @Test
        fun `Returns the right candidate when it does not have a number if previous had a number`() {
            val candidateWithNumber = Candidate("Eetu", "Ehdokas", 2, Party.SIT)
            val candidateWithoutNumber = Candidate("Eetu", "Ehdokas", 0, Party.SIT)
            assertEquals(candidateWithNumber, Candidates.putIfAbsent(Municipality.LEMPAALA, candidateWithNumber))
            assertEquals(candidateWithNumber, Candidates.putIfAbsent(Municipality.LEMPAALA, candidateWithoutNumber))
        }

        @Test
        fun `Returns the right candidate when it has a number but previous did not have a number`() {
            val candidateWithoutNumber = Candidate("Eetu", "Ehdokas", 0, Party.SIT)
            val candidateWithNumber = Candidate("Eetu", "Ehdokas", 2, Party.SIT)
            assertEquals(candidateWithoutNumber, Candidates.putIfAbsent(Municipality.LEMPAALA, candidateWithoutNumber))
            assertEquals(candidateWithNumber, Candidates.putIfAbsent(Municipality.LEMPAALA, candidateWithNumber))
        }

        @Test
        fun `Returns the right candidate when it has different first name`() {
            val candidateWithNumber = Candidate("Eetu", "Ehdokas", 2, Party.SIT)
            val candidateWithoutNumber = Candidate("Eetu 'Erinomainen'", "Ehdokas", 2, Party.SIT)
            assertEquals(candidateWithNumber, Candidates.putIfAbsent(Municipality.LEMPAALA, candidateWithNumber))
            assertEquals(candidateWithNumber, Candidates.putIfAbsent(Municipality.LEMPAALA, candidateWithoutNumber))
        }
    }
}