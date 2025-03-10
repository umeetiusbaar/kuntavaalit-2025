package fi.lempimetsa.kuntavaalit2025.yle

import fi.lempimetsa.kuntavaalit2025.med
import fi.lempimetsa.kuntavaalit2025.yle.Constituency.Companion.CONSTITUENCIES
import java.util.SortedMap
import java.util.TreeMap

val yleService = YleService()

suspend fun main() {
    print()
}

suspend fun print() {
    CONSTITUENCIES.forEach { constituency ->
        val questions = yleService.questions(constituency)
        val answers = yleService.answers(constituency, questions)
        println(constituency.name)
        println()
        printMunicipality(questions, answers)
        println()
    }
}

private fun printMunicipality(questions: List<Question>, answers: SortedMap<Candidate, SortedMap<Question, Answer>>) {
    printQuestionRow(questions)
    printCandidateAnswerRows(answers)
    printMedianAnswers(answers)
    printYeaNayEmptyAway(answers)
    printPartyMedianAnswers(answers)
}

private fun printQuestionRow(questions: List<Question>) {
    val questionsRow = "\t\t${questions.joinToString("\t", transform = { it.text })}"
    println(questionsRow)
}

private fun printCandidateAnswerRows(answers: SortedMap<Candidate, SortedMap<Question, Answer>>) {
    answers.forEach { answerEntry ->
        val candidate = answerEntry.key
        val answers = answerEntry.value
        val answerRow = "${candidate.fullName()}\t${candidate.party.shortName}\t${
            answers.values.joinToString(
                "\t",
                transform = { it.answer.toString() })
        }"
        println(answerRow)
    }
}

private fun printMedianAnswers(
    answers: SortedMap<Candidate, SortedMap<Question, Answer>>
) {
    val medianAnswers = sortedMapOf<Question, MutableList<Int>>()
    answers.values.forEach { questionAndAnswers ->
        questionAndAnswers.entries.forEach { questionAndAnswer ->
            val question = questionAndAnswer.key
            val answer = questionAndAnswer.value
            val counter = medianAnswers.computeIfAbsent(question) { mutableListOf() }
            counter.add(answer.answer)
        }
    }
    println()
    println("Ehdokkaat keskimäärin\t\t${medianAnswers.values.joinToString("\t") { med(it).toString() }}")
}

private fun printYeaNayEmptyAway(answers: SortedMap<Candidate, SortedMap<Question, Answer>>) {
    val yeaNayEmptyAway = sortedMapOf<Question, IntArray>()
    answers.values.forEach { questionAndAnswers ->
        questionAndAnswers.entries.forEach { questionAndAnswer ->
            val question = questionAndAnswer.key
            val answer = questionAndAnswer.value
            val array = yeaNayEmptyAway.computeIfAbsent(question) { IntArray(2) }
            when {
                answer.agree() -> {
                    array[0]++
                }
                else -> {
                    array[1]++
                }
            }
        }
    }
    println()
    println("Jaa / Ei\t\t${yeaNayEmptyAway.values.joinToString("\t") { it.joinToString(" / ") }}")
}

private fun printPartyMedianAnswers(answers: SortedMap<Candidate, SortedMap<Question, Answer>>) {
    val partyMedianAnswers = sortedMapOf<Question, SortedMap<Party, MutableList<Int>>>()
    answers.entries.groupBy { it.key.party }.forEach { (party, candidateQuestionsAndAnswers) ->
        candidateQuestionsAndAnswers.forEach { questionsAndAnswer ->
            questionsAndAnswer.value.forEach { questionAndAnswer ->
                val question = questionAndAnswer.key
                val answer = questionAndAnswer.value
                val partyCounters = partyMedianAnswers.computeIfAbsent(question) { TreeMap() }
                val counter = partyCounters.computeIfAbsent(party) { mutableListOf() }
                counter.add(answer.answer)
            }
        }
    }
    val parties = partyMedianAnswers.values.first().keys.toSortedSet()
    println()
    parties.forEach { party ->
        println("${party.shortName}\t\t${partyMedianAnswers.values.joinToString("\t") { med(it[party]!!).toString() }}")
    }
}
