package fi.lempimetsa.kuntavaalit2025.facebook

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import fi.lempimetsa.kuntavaalit2025.FacebookQuestionOption

class FacebookHtmlParser {

    fun parseNames(option: FacebookQuestionOption): List<Pair<String, String>> {
        val names = mutableListOf<Pair<String, String>>()
        val parser = initializeParser(names)
        val html = when (option) {
            FacebookQuestionOption.PUSKIAISTEN_OIKAISU -> javaClass.getResourceAsStream("/fb-puskiaisten-oikaisu.html")!!
                .bufferedReader().readText()

            FacebookQuestionOption.KEHATIEN_JATKO -> javaClass.getResourceAsStream("/fb-kehatien-jatko.html")!!
                .bufferedReader().readText()

            FacebookQuestionOption.JARJESTELYRATAPIHA -> javaClass.getResourceAsStream("/fb-jarjestelyratapiha.html")!!
                .bufferedReader().readText()

            FacebookQuestionOption.RAUTATIETUNNELI -> javaClass.getResourceAsStream("/fb-rautatietunneli.html")!!
                .bufferedReader().readText()

            FacebookQuestionOption.EI_MITAAN -> javaClass.getResourceAsStream("/fb-ei-mitaan.html")!!
                .bufferedReader().readText()
        }
        parser.write(html)
        parser.end()
        return names
    }

    private fun initializeParser(names: MutableList<Pair<String, String>>): KsoupHtmlParser {
        val handler = KsoupHtmlHandler.Builder().onText { text ->
            if (text.isNameText()) {
                val cleanedText = text.cleaned()
                val firstName = cleanedText.substringBeforeLast(' ')
                val lastName = cleanedText.substringAfterLast(' ')
                names += Pair(firstName, lastName)
            }
        }.build()
        return KsoupHtmlParser(handler = handler)
    }

    private fun String.isNameText() =
        this.isNotBlank() && this != "Lisää kaveriksi" && !this.contains("yhteinen kaveri") && !this.contains("yhteistä kaveria") && this != "Viesti"

    private fun String.cleaned() = when {
        this.contains(" - ") -> this.substringBefore(" - ")
        this.contains(" Os, ") -> this.substringBefore(" Os, ")
        else -> this
    }
}

