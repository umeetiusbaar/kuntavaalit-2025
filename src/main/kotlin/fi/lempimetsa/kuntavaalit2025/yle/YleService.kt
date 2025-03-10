package fi.lempimetsa.kuntavaalit2025.yle

import java.util.SortedMap
import java.util.TreeMap

class YleService {

    private val client = YleClient()

    suspend fun questions(constituency: Constituency): List<Question> =
        client.questions(constituency.id).flatMap { it.questions }

    suspend fun answers(
        constituency: Constituency,
        questions: List<Question>
    ): SortedMap<Candidate, SortedMap<Question, Answer>> {
        val answers = sortedMapOf<Candidate, SortedMap<Question, Answer>>()
        val candidates = client.candidates(constituency.id)
        val parties = client.parties(constituency.id).associateBy { it.id }
        candidates.forEach { c ->
            var candidate = client.candidate(constituency.id, c.id)
            // set party for candidate
            candidate = candidate.copy(
                party = parties[candidate.partyId] ?: Party.EMPTY
            )
            val candidateAnsers = answers.computeIfAbsent(candidate) { TreeMap() }
            questions.forEach { question ->
                val answer = candidate.answers?.get(question.id.toString()) ?: Answer.EMPTY
                candidateAnsers.put(question, answer)
            }
        }
        return answers
    }
}
