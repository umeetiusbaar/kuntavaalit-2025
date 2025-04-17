package fi.lempimetsa.kuntavaalit2025

import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import kotlin.collections.component1
import kotlin.collections.component2

val INTERESTING_QUESTION_FILTER: (Question) -> Boolean =
    { it.title.contains("puskiais", true) || it.title.contains("sääksjärv", true) }

private val service = VaalikoneAnswersService()

suspend fun main(args: Array<String>) {
    val municipalities = if (args.isEmpty()) Municipality.entries else args[0].split(",").map { Municipality.valueOf(it) }
    municipalities.forEach { municipality ->
        val questionsAndAnswers = service.questionsAndAnswers(municipality, INTERESTING_QUESTION_FILTER)
        val results = service.results(municipality)
        println(municipality.title)
        printQuestionsAndAnswers(questionsAndAnswers, results)
        println()
    }
}

fun printQuestionsAndAnswers(questionsAndAnswers: Pair<List<Question>, SortedMap<Candidate, SortedMap<Question, Answer>>>, results: Map<Int, Result>) {
    val questions = questionsAndAnswers.first.sorted()
    val answers = questionsAndAnswers.second
    printQuestionsRow(questions)
    printCandidateAnswers(questions, answers, results)
    printMedianAnswers(answers, results)
    printPartyMedians(answers, results)
}

fun printQuestionsRow(questions: List<Question>) {
    val questionsRow = "Nimi\tPuolue\tÄänestysnumero\tValittu\t${
        questions.joinToString(
            "\t",
            transform = { "${it.source.name} : ${it.title}" })
    }"
    println(questionsRow)
}

fun printCandidateAnswers(
    questions: List<Question>,
    answers: SortedMap<Candidate, SortedMap<Question, Answer>>,
    results: Map<Int, Result>
) {
    answers.forEach { answerEntry ->
        val candidate = answerEntry.key
        val result = results[candidate.number] ?: Result.NOT_ELECTED
        if (candidate.number > 1) {
            print("${candidate.fullName()}\t${candidate.party}\t${candidate.number}\t${result}")
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

fun printMedianAnswers(answers: SortedMap<Candidate, SortedMap<Question, Answer>>, results: Map<Int, Result>) {
    val medianAnswers = sortedMapOf<Question, MutableList<Int>>()
    val electedMedianAnswers = sortedMapOf<Question, MutableList<Int>>()
    answers.forEach { answerEntry ->
        val candidate = answerEntry.key
        val result = results[candidate.number] ?: Result.NOT_ELECTED
        if (candidate.number > 1) {
            answerEntry.value.entries.forEach { questionAndAnswer ->
                val question = questionAndAnswer.key
                if (question.source != Source.FACEBOOK) {
                    val answer = questionAndAnswer.value
                    if (answer.value > 0) {
                        val counter = medianAnswers.computeIfAbsent(question) { mutableListOf() }
                        counter.add(answer.value)
                        if (result == Result.ELECTED) {
                            val electedCounter = electedMedianAnswers.computeIfAbsent(question) { mutableListOf() }
                            electedCounter.add(answer.value)
                        }
                    }
                }
            }
        }
    }
    println()
    println("Ehdokkaiden keskiarvo\t\t\t\t${medianAnswers.values.joinToString("\t") { String.format("%.1f", it.avg()) }}")
    println("Valittujen keskiarvo\t\t\t\t${electedMedianAnswers.values.joinToString("\t") { String.format("%.1f", it.avg()) }}")
    println("Ehdokkaiden mediaani\t\t\t\t${medianAnswers.values.joinToString("\t") { String.format("%d", it.med()) }}")
    println("Valittujen mediaani\t\t\t\t${electedMedianAnswers.values.joinToString("\t") { String.format("%d", it.med()) }}")
}

fun printPartyMedians(
    answers: SortedMap<Candidate, SortedMap<Question, Answer>>,
    results: Map<Int, Result>,
) {
    var parties = answers.keys.map { it.party }.distinct().toSortedSet()

    // candidates
    val answersByParty = answers.entries.filter { it.key.number > 1 }.groupBy { it.key.party }
    printPartyMedians(answersByParty, parties)

    println()

    // elected
    val electedAnswersByParty =
        answers.entries.filter { it.key.number > 1 && results.isElected(it.key.number) }.groupBy { it.key.party }
    printPartyMedians(electedAnswersByParty, parties)
}

fun printPartyMedians(answersByParty: Map<Party, List<MutableMap.MutableEntry<Candidate, SortedMap<Question, Answer>>>>, parties: SortedSet<Party>) {
    var partyMedianAnswers = sortedMapOf<Question, SortedMap<Party, MutableList<Int>>>()
    answersByParty
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
    println()
    parties.forEach { party ->
        println(
            "$party\t\t\t\t${
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

private fun Map<Int, Result>.isElected(number: Int) = this[number] == Result.ELECTED
