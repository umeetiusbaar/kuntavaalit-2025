package fi.lempimetsa.kuntavaalit2025

import fi.lempimetsa.kuntavaalit2025.aamulehti.AamulehtiService
import fi.lempimetsa.kuntavaalit2025.facebook.FacebookService
import fi.lempimetsa.kuntavaalit2025.lvs.LVSService
import fi.lempimetsa.kuntavaalit2025.mtv.MTVService
import fi.lempimetsa.kuntavaalit2025.pirkkalainen.PirkkalainenService
import fi.lempimetsa.kuntavaalit2025.tamperelainen.TamperelainenService
import fi.lempimetsa.kuntavaalit2025.yle.ElectedInformation
import fi.lempimetsa.kuntavaalit2025.yle.YleService
import java.util.SortedMap
import java.util.TreeMap

class VaalikoneAnswersService {

    private val lvsService = LVSService()
    private val pirkkalainenService = PirkkalainenService()
    private val tamperelainenService = TamperelainenService()
    private val aamulehtiService = AamulehtiService()
    private val yleService = YleService()
    private val mtvService = MTVService()
    private val facebookService = FacebookService()

    suspend fun questionsAndAnswers(
        municipality: Municipality,
        questionFilter: (Question) -> Boolean = { _ -> true }
    ): Pair<List<Question>, SortedMap<Candidate, SortedMap<Question, Answer>>> {
        val lvsQuestionsAndAnswers = lvsService.questionsAndAnswers(municipality, questionFilter)
        val pirkkalainenQuestionsAndAnswers = pirkkalainenService.questionsAndAnswers(municipality, questionFilter)
        val tamperelainenQuestionsAndAnswers = tamperelainenService.questionsAndAnswers(municipality, questionFilter)
        val aamulehtiQuestionsAndAnswers = aamulehtiService.questionsAndAnswers(municipality, questionFilter)
        val yleQuestionsAndAnswers = yleService.questionsAndAnswers(municipality, questionFilter)
        val mtvQuestionsAndAnswers = mtvService.questionsAndAnswers(municipality, questionFilter)
        val allCandidates =
            (lvsQuestionsAndAnswers?.second?.keys ?: emptySet()) + (pirkkalainenQuestionsAndAnswers?.second?.keys
                ?: emptySet()) +
                    (tamperelainenQuestionsAndAnswers?.second?.keys
                        ?: emptySet()) + (aamulehtiQuestionsAndAnswers?.second?.keys ?: emptySet()) +
                    (yleQuestionsAndAnswers?.second?.keys ?: emptySet()) + (mtvQuestionsAndAnswers?.second?.keys
                ?: emptySet())
        val facebookQuestionsAndAnswers = facebookService.questionsAndAnswers(allCandidates)
        val questions = mutableListOf<Question>()
        questions.addAll(lvsQuestionsAndAnswers?.first ?: emptyList())
        questions.addAll(pirkkalainenQuestionsAndAnswers?.first ?: emptyList())
        questions.addAll(tamperelainenQuestionsAndAnswers?.first ?: emptyList())
        questions.addAll(aamulehtiQuestionsAndAnswers?.first ?: emptyList())
        questions.addAll(yleQuestionsAndAnswers?.first ?: emptyList())
        questions.addAll(mtvQuestionsAndAnswers?.first ?: emptyList())
        questions.addAll(facebookQuestionsAndAnswers.first)
        val candidatesAndAnswers = TreeMap<Candidate, SortedMap<Question, Answer>>()
        candidatesAndAnswers.addAll(lvsQuestionsAndAnswers?.second)
        candidatesAndAnswers.addAll(pirkkalainenQuestionsAndAnswers?.second)
        candidatesAndAnswers.addAll(tamperelainenQuestionsAndAnswers?.second)
        candidatesAndAnswers.addAll(aamulehtiQuestionsAndAnswers?.second)
        candidatesAndAnswers.addAll(yleQuestionsAndAnswers?.second)
        candidatesAndAnswers.addAll(mtvQuestionsAndAnswers?.second)
        candidatesAndAnswers.addAll(facebookQuestionsAndAnswers.second)
        return Pair(questions, candidatesAndAnswers)
    }

    suspend fun results(municipality: Municipality): Map<Int, Result> {
        val candidateResults = yleService.candidateResults(municipality)
        return candidateResults.associate { candidateResult ->
            val result =
            if (municipality == Municipality.LEMPAALA) {
                if (candidateResult.caid == 26) {
                    // Tirkkonen (kesk.) is not elected
                    candidateResult.copy(26, ElectedInformation.NOT_ELECTED)
                } else if (candidateResult.caid == 24) {
                    // Tappura (kesk.) is elected
                    candidateResult.copy(24, ElectedInformation.ELECTED)
                } else candidateResult
            } else if (municipality == Municipality.PIRKKALA) {
                if (candidateResult.caid == 21) {
                    // Auer (kesk.) is not elected
                    candidateResult.copy(21, ElectedInformation.NOT_ELECTED)
                } else if (candidateResult.caid == 28) {
                    // Naskali (kesk.) is elected
                    candidateResult.copy(28, ElectedInformation.ELECTED)
                } else candidateResult
            } else candidateResult
            Pair(result.caid, Result.valueOfResults(result.electedInformation.isElected, result.electedInformation.isOnSubstitutePlace))
        }
    }
}

private fun TreeMap<Candidate, SortedMap<Question, Answer>>.addAll(
    map: SortedMap<Candidate, SortedMap<Question, Answer>>?,
) {
    map?.keys?.forEach { candidate ->
        val questionsAndAnswers = this.computeIfAbsent(candidate) { TreeMap() }
        questionsAndAnswers.putAll(map[candidate]!!)
    }
}
