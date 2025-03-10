package fi.lempimetsa.kuntavaalit2025.aamulehti

import java.util.SortedMap

class AamulehtiService {

    val client = AamulehtiClient()

    /**
     * Returns all questions that are common or related to the given municipality.
     */
    suspend fun questions(municipality: Municipality, brand: Brand): List<Question> =
        client.candidates().questions.filter { it.isMunicipalityTheme(municipality) && it.isBrand(brand) }.sortedBy { it.id }

    /**
     * Returns answers for the given municipality and questions.
     */
    suspend fun answers(
        municipality: Municipality,
        questions: List<Question> = emptyList()
    ): SortedMap<Candidate, SortedMap<Question, Answer>> {
        val answers = sortedMapOf<Candidate, SortedMap<Question, Answer>>()
        val parties = client.candidates().parties.associateBy { it.id }
        val candidates = client.candidateSearch(municipality)
        candidates.forEach { candidate ->
            val candidateQuestions = client.candidateQuestions(candidate)
            val party = parties[candidate.party] ?: error("Party ${candidate.party} not found for candidate ${candidate.id}")
            val candidate = candidate.copy(party = party.shortName)
            answers.put(
                candidate,
                candidateQuestions.answersForQuestion(questions))
        }
        return answers
    }
}
