package fi.lempimetsa.kuntavaalit2025.facebook

import fi.lempimetsa.kuntavaalit2025.FacebookQuestionOption
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FacebookHtmlParserTest {

    private val parser = FacebookHtmlParser()

    @Nested
    inner class ParseNames {
        @Test
        fun `parse Puskiaisten oikaisu supporter names`() {
            val names = parser.parseNames(FacebookQuestionOption.PUSKIAISTEN_OIKAISU)
            println(names.size)
            names.forEach { println(it) }
        }

        @Test
        fun `parse 2-kehatie supporter names`() {
            val names = parser.parseNames(FacebookQuestionOption.KEHATIEN_JATKO)
            println(names.size)
            names.forEach { println(it) }
        }

        @Test
        fun `parse jarjestelyratapiha supporter names`() {
            val names = parser.parseNames(FacebookQuestionOption.JARJESTELYRATAPIHA)
            println(names.size)
            names.forEach { println(it) }
        }

        @Test
        fun `parse rautatietunneli supporter names`() {
            val names = parser.parseNames(FacebookQuestionOption.RAUTATIETUNNELI)
            println(names.size)
            names.forEach { println(it) }
        }

        @Test
        fun `parse nothing supporter names`() {
            val names = parser.parseNames(FacebookQuestionOption.EI_MITAAN)
            println(names.size)
            names.forEach { println(it) }
        }
    }
}