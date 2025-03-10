package fi.lempimetsa.kuntavaalit2025.aamulehti

import fi.lempimetsa.kuntavaalit2025.med
import java.util.SortedMap
import java.util.TreeMap

val aamulehtiService = AamulehtiService()

suspend fun main() {
    printAamulehti()
}

suspend fun printAamulehti() {
    Municipality.entries.forEach { municipality ->
        val questions = aamulehtiService.questions(municipality, Brand.AAMULEHTI)
        val answers = aamulehtiService.answers(municipality, questions)
        println(municipality.label)
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
        val answerRow = "${candidate.fullName()}\t${candidate.party}\t${
            answers.values.joinToString(
                "\t",
                transform = { it.value.toString() })
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
            counter.add(answer.value)
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
            val array = yeaNayEmptyAway.computeIfAbsent(question) { IntArray(4) }
            when (answer) {
                Answer.SOMEWHAT_AGREE -> array[0]++
                Answer.STRONGLY_AGREE -> array[0]++
                Answer.SOMEWHAT_DISAGREE -> array[1]++
                Answer.STRONGLY_DISAGREE -> array[1]++
                Answer.NEITHER_AGREE_NOR_DISAGREE -> array[2]++
                Answer.NOT_ANSWERED -> array[3]++
            }
        }
    }
    println()
    println("Jaa / Ei / Tyhjiä / Poissa\t\t${yeaNayEmptyAway.values.joinToString("\t") { it.joinToString(" / ") }}")
}

private fun printPartyMedianAnswers(answers: SortedMap<Candidate, SortedMap<Question, Answer>>) {
    val partyMedianAnswers = sortedMapOf<Question, SortedMap<String, MutableList<Int>>>()
    answers.entries.groupBy { it.key.party }.forEach { (party, candidateQuestionsAndAnswers) ->
        candidateQuestionsAndAnswers.forEach { questionsAndAnswer ->
            questionsAndAnswer.value.forEach { questionAndAnswer ->
                val question = questionAndAnswer.key
                val answer = questionAndAnswer.value
                val partyCounters = partyMedianAnswers.computeIfAbsent(question) { TreeMap() }
                val counter = partyCounters.computeIfAbsent(party) { mutableListOf() }
                counter.add(answer.value)
            }
        }
    }
    val parties = partyMedianAnswers.values.first().keys.toSortedSet()
    println()
    parties.forEach { party ->
        println("$party\t\t${partyMedianAnswers.values.joinToString("\t") { med(it[party]!!).toString() }}")
    }
}
