package fi.lempimetsa.kuntavaalit2025

import java.util.SortedMap
import java.util.TreeMap
import kotlin.collections.component1
import kotlin.collections.component2

val INTERESTING_QUESTION_FILTER: (Question) -> Boolean =
    { it.title.contains("puskiais", true) || it.title.contains("sääksjärv", true) }

private val service = VaalikoneResultsService()

suspend fun main() {
    Municipality.entries.forEach { municipality ->
        val questionsAndAnswers = service.questionsAndAnswers(municipality, INTERESTING_QUESTION_FILTER)
        println(municipality.title)
        printQuestionsAndAnswers(questionsAndAnswers)
        println()
    }
}

fun printQuestionsAndAnswers(questionsAndAnswers: Pair<List<Question>, SortedMap<Candidate, SortedMap<Question, Answer>>>) {
    val questions = questionsAndAnswers.first.sorted()
    val answers = questionsAndAnswers.second
    printQuestionsRow(questions)
    printCandidateAnswers(questions, answers)
    printMedianAnswers(answers)
    printPartyMedians(answers)
}

fun printQuestionsRow(questions: List<Question>) {
    val questionsRow = "Nimi\tPuolue\tÄänestysnumero\t${
        questions.joinToString(
            "\t",
            transform = { "${it.source.name} : ${it.title}" })
    }"
    println(questionsRow)
}

fun printCandidateAnswers(
    questions: List<Question>,
    answers: SortedMap<Candidate, SortedMap<Question, Answer>>
) {
    answers.forEach { answerEntry ->
        val candidate = answerEntry.key
        if (candidate.number > 1) {
            print("${candidate.fullName()}\t${candidate.party}\t${candidate.number}")
            val answers = answerEntry.value
            questions.forEach { question ->
                val answer = answers[question]
                if (answer == null) {
                    print("\t")
                } else
                    if (question.source == Source.FACEBOOK) {
                        if (answer.value == 10) {
                            print("\tx")
                        } else {
                            print("\t")
                        }
                    } else {
                        print("\t$answer")
                    }
            }
            println()
        }
    }
}

fun printMedianAnswers(answers: SortedMap<Candidate, SortedMap<Question, Answer>>) {
    val medianAnswers = sortedMapOf<Question, MutableList<Int>>()
    answers.forEach { answerEntry ->
        val candidate = answerEntry.key
        if (candidate.number > 1) {
            answerEntry.value.entries.forEach { questionAndAnswer ->
                val question = questionAndAnswer.key
                if (question.source != Source.FACEBOOK) {
                    val answer = questionAndAnswer.value
                    if (answer.value > 0) {
                        val counter = medianAnswers.computeIfAbsent(question) { mutableListOf() }
                        counter.add(answer.value)
                    }
                }
            }
        }
    }
    println()
    println("Ehdokkaat keskimäärin\t\t\t${medianAnswers.values.joinToString("\t") { String.format("%.1f", it.avg()) }}")
}

fun printPartyMedians(
    answers: SortedMap<Candidate, SortedMap<Question, Answer>>
) {
    val partyMedianAnswers = sortedMapOf<Question, SortedMap<Party, MutableList<Int>>>()
    answers.entries.filter { it.key.number > 1 }.groupBy { it.key.party }
        .forEach { (party, candidateQuestionsAndAnswers) ->
            candidateQuestionsAndAnswers.forEach { questionsAndAnswer ->
                questionsAndAnswer.value.forEach { questionAndAnswer ->
                    val question = questionAndAnswer.key
                    val answer = questionAndAnswer.value
                    if (answer.value > 0) {
                        val partyCounters = partyMedianAnswers.computeIfAbsent(question) { TreeMap() }
                        val counter = partyCounters.computeIfAbsent(party) { mutableListOf() }
                        counter.add(answer.value)
                    }
                }
            }
        }
    val parties = answers.keys.map { it.party }.distinct().toSortedSet()
    println()
    parties.forEach { party ->
        println(
            "$party\t\t\t${
                partyMedianAnswers.values.joinToString("\t") {
                    val med = it[party].avg()
                    when {
                        med == 10.0 -> "x"
                        med == 11.0 -> ""
                        med > 0 -> String.format("%.1f", med)
                        else -> ""
                    }
                }
            }"
        )
    }
}
