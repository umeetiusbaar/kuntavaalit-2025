package fi.lempimetsa.kuntavaalit2025.yle

import fi.lempimetsa.kuntavaalit2025.Candidates
import fi.lempimetsa.kuntavaalit2025.Municipality
import fi.lempimetsa.kuntavaalit2025.Source
import fi.lempimetsa.kuntavaalit2025.VaalikoneService
import fi.lempimetsa.kuntavaalit2025.adjustFirstName
import fi.lempimetsa.kuntavaalit2025.adjustLastName
import java.util.SortedMap
import java.util.TreeMap

class YleService : VaalikoneService {

    private val client = YleClient()

    companion object {
        val SOURCE = Source.YLE
    }

    override suspend fun questionsAndAnswers(
        municipality: Municipality,
        questionFilter: (fi.lempimetsa.kuntavaalit2025.Question) -> Boolean
    ): Pair<List<fi.lempimetsa.kuntavaalit2025.Question>, SortedMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>>? {
        val sourceConstituency = Constituency.valueOfName(municipality.title)
        val sourceQuestions = questions(sourceConstituency)
        val sourceAnswers = answers(sourceConstituency, sourceQuestions)
        val questions =
            sourceQuestions.map { fi.lempimetsa.kuntavaalit2025.Question(SOURCE, it.id, it.text) }
                .filter { questionFilter(it) }
        val answers =
            TreeMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>()
        sourceAnswers.forEach { (sourceCandidate, sourceAnswers) ->
            val party = fi.lempimetsa.kuntavaalit2025.Party.valueOfShortName(sourceCandidate.party.shortName)
                ?: throw IllegalStateException("Party ${sourceCandidate.party.shortName} not found")
            var candidate = fi.lempimetsa.kuntavaalit2025.Candidate(
                sourceCandidate.firstName.adjustFirstName(municipality, sourceCandidate.lastName.trim()),
                sourceCandidate.lastName.adjustLastName(municipality, party, sourceCandidate.firstName.trim()),
                sourceCandidate.electionNumber,
                party
            )
            candidate = Candidates.putIfAbsent(municipality, candidate)
            val candidateAnswers = sourceAnswers.mapNotNull { (question, answer) ->
                val question = questions.find { it.id == question.id }
                if (question != null) {
                    val answer = fi.lempimetsa.kuntavaalit2025.Answer(question, answer.answer, answer.explanation())
                    Pair(question, answer)
                } else {
                    null
                }
            }.toMap()
            answers.computeIfAbsent(candidate) { TreeMap() }.putAll(candidateAnswers)
        }
        return Pair(questions, answers)
    }

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

    suspend fun candidateResults(municipality: Municipality): List<CandidateResult> {
        val constituency = Constituency.valueOfName(municipality.title)
        val municipalityId = constituency.officialId.toInt()
        return client.candidateResults(municipalityId).data.candidateResults
    }
}
