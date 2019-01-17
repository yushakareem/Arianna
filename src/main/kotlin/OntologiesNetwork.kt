import java.util.*
import kotlin.concurrent.fixedRateTimer

class OntologiesNetwork {

    fun startNetworking(vararg ontologyLinks: OntologyLinks) = startNetworking(ontologyLinks.asList())

    private fun startNetworking(ontologyLinks: List<OntologyLinks>): Handler {

        val fixedRateTimer = fixedRateTimer(name = "reasoning-layer", initialDelay = 0, period = 1000) {
            coreComputations()
        }

        return Handler(fixedRateTimer)
    }

    private fun coreComputations() {
        println("The core manipulation and reasoning over network of ontologies happens here!")
    }

    class Handler(private val fixedRateTimer: Timer) {

        fun stop() {
                fixedRateTimer.cancel()
        }
    }
}