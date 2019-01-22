import java.util.*
import kotlin.concurrent.fixedRateTimer

class OntologiesNetwork {



    fun startNetworking(vararg ontologyLinksConfig: OntologyLinksConfiguration) = startNetworking(ontologyLinksConfig.asList())

    private fun startNetworking(ontologyLinksConfig: List<OntologyLinksConfiguration>): Handler {

        lateinit var fixedRateTimer: Timer

        ontologyLinksConfig.listIterator().forEach {

            if (it.isActivatedByScheduler) {

                fixedRateTimer = fixedRateTimer(name = "PeriodicNonDaemonThread", initialDelay = it.schedulerInitialDelay, period = it.schedulerIntervalPeriod) {
                    coreComputations(it)
                }
            }
        }

        return Handler(fixedRateTimer)
    }

    private fun coreComputations(it: OntologyLinksConfiguration) {
        println("This is it: ${it.ontoAtCenterOfLinks.getOntoRef()}")
    }

    class Handler(private val fixedRateTimer: Timer) {

        fun stop() {
                fixedRateTimer.cancel()
        }
    }
}