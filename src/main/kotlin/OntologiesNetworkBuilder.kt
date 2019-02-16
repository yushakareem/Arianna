import it.emarolab.amor.owlDebugger.Logger
/**
 * A builder class for building an object of class OntologiesNetwork().
 * Allows to initialize the Network with some particular features.
 *
 * @return OntologiesNetwork
 */

class OntologiesNetworkBuilder {

    // Feature to disable OWLOOP analytics
    fun withOWLOOPAnalyticsDisabled(): OntologiesNetworkBuilder {

        Logger.LoggerFlag.resetAllLoggingFlags()
        return this
    }

    // Feature to disable Arianna analytics
//    fun withAriannaAnalyticsDisabled(): OntologiesNetworkBuilder {}

    fun build(): OntologiesNetwork {

        return OntologiesNetwork()
    }

}