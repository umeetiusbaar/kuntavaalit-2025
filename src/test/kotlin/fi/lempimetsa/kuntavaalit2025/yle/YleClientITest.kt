package fi.lempimetsa.kuntavaalit2025.yle

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class YleClientITest {

    private val client: YleClient = YleClient()

    @Test
    @Disabled("Only for manual testing")
    fun constituencies() = runBlocking {
        client.constituencies().forEach { println(it) }
    }

    @Test
    @Disabled("Only for manual testing")
    fun candidates() = runBlocking {
        client.candidates(114).forEach { println(it) }
    }

    @Test
    @Disabled("Only for manual testing")
    fun parties() = runBlocking {
        client.parties(114).forEach { println(it) }
    }

    @Test
    @Disabled("Only for manual testing")
    fun questions() = runBlocking {
        client.questions(114).forEach { println(it) }
    }

    @Test
    @Disabled("Only for manual testing")
    fun candidate() = runBlocking {
        println(client.candidate(114, 5208))
    }
}