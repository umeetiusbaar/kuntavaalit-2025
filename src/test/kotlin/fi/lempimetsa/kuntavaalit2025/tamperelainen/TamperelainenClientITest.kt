package fi.lempimetsa.kuntavaalit2025.tamperelainen

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class TamperelainenClientITest {

    private val client = TamperelainenClient()

    @Test
    fun district() = runBlocking {
        println(client.district(Election.KUNTAVAALIT.id, District.TAMPERE.id))
    }
}
