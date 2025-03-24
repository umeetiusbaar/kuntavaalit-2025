package fi.lempimetsa.kuntavaalit2025.tamperelainen

import fi.lempimetsa.kuntavaalit2025.Candidates
import fi.lempimetsa.kuntavaalit2025.Municipality
import fi.lempimetsa.kuntavaalit2025.Source
import fi.lempimetsa.kuntavaalit2025.VaalikoneService
import fi.lempimetsa.kuntavaalit2025.adjustFirstName
import fi.lempimetsa.kuntavaalit2025.adjustLastName
import java.util.SortedMap
import java.util.TreeMap

class TamperelainenService : VaalikoneService {

    private val client = TamperelainenClient()

    companion object {
        val SOURCE = Source.TAMPERELAINEN
    }

    override suspend fun questionsAndAnswers(
        municipality: Municipality,
        questionFilter: (fi.lempimetsa.kuntavaalit2025.Question) -> Boolean
    ): Pair<List<fi.lempimetsa.kuntavaalit2025.Question>, SortedMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>>? {
        val election = Election.KUNTAVAALIT
        val district = District.valueOfName(municipality.title)
        if (district == null) return null
        val (sourceQuestions, sourceParties) = questionsAndParties(election, district)
        val sourceAnswers = answers(election, district, sourceQuestions, sourceParties)
        val questions = sourceQuestions.map { fi.lempimetsa.kuntavaalit2025.Question(SOURCE, it.id, it.text()) }
            .filter { questionFilter(it) }
        val answers =
            TreeMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>()
        sourceAnswers.forEach { (sourceCandidate, sourceAnswers) ->
            val party = fi.lempimetsa.kuntavaalit2025.Party.valueOfShortName(sourceCandidate.party.shortName())
                ?: throw IllegalStateException("Party ${sourceCandidate.party.shortName()} not found")
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

    suspend fun questionsAndParties(election: Election, district: District): Pair<List<Question>, List<Party>> {
        val districtResponses = client.district(election.id, district.id)
        return Pair(districtResponses.questions, districtResponses.parties)
    }

    suspend fun answers(
        election: Election,
        district: District,
        questions: List<Question>,
        parties: List<Party>,
    ): SortedMap<Candidate, SortedMap<Question, Answer>> {
        val answers = sortedMapOf<Candidate, SortedMap<Question, Answer>>()
        val candidates = client.candidates(election.id, district.id)
        val parties = parties.associateBy { it.id }
        candidates.forEach { c ->
            var candidate = client.candidate(election.id, district.id, c.id).candidate
            // set party for candidate
            candidate = candidate.copy(
                party = parties[candidate.partyId] ?: Party.Companion.EMPTY
            )
            val candidateAnsers = answers.computeIfAbsent(candidate) { TreeMap() }
            questions.forEach { question ->
                val answer = candidate.answers.find { it.questionId == question.id } ?: Answer.Companion.EMPTY
                candidateAnsers.put(question, answer)
            }
        }
        return answers
    }
}
