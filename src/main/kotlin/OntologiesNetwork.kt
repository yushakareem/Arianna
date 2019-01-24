import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

class OntologiesNetwork {

    fun startNetworking(vararg ontologyLinksConfig: OntologyLinksConfiguration) = startNetworking(ontologyLinksConfig.asList())

    private fun startNetworking(ontologyLinksConfig: List<OntologyLinksConfiguration>): OntologiesNetworkHandler {

        lateinit var fixedRateTimer: Timer

        ontologyLinksConfig.listIterator().forEach {

            if (it.isActivatedByScheduler) {

                fixedRateTimer = fixedRateTimer(name = "PeriodicNonDaemonThread", initialDelay = it.schedulerInitialDelay, period = it.schedulerIntervalPeriod) {
                    coreComputations(it)
                }
            }
        }

        return OntologiesNetworkHandler(fixedRateTimer)
    }

    private fun coreComputations(it: OntologyLinksConfiguration) {

        transferSensorsData(it.inputDBInfo, it.mapDBTableToStatement, it.ontoAtCenterOfLinks)
    }

    private fun transferSensorsData(inputDBInfo: MySqlConnector, mapDBTableToStatement: HashMap<String, IncompleteStatement>, ontoAtCenterOfLinks: Ontology) {

        mapDBTableToStatement.iterator().forEach {

            lateinit var sensorValue: String
            lateinit var timeStamp: Timestamp

            inputDBInfo.connectToDBorCreateNewDB()
            val resultSet = inputDBInfo.readLatestRow(it.key)
            if (resultSet.next()) {
                sensorValue = inputDBInfo.getStringValue(resultSet)
                timeStamp = inputDBInfo.getTimestamp(resultSet)
            }
            val sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
            val sensorTimestampStatement = DataPropertyStatement(it.value.getSubject(), "hasTimestamp", timeStamp)
            ontoAtCenterOfLinks.addOrUpdateToOnto(sensorValueStatement)
            ontoAtCenterOfLinks.addOrUpdateToOnto(sensorTimestampStatement)
            inputDBInfo.disconnectFromDB()
            ontoAtCenterOfLinks.saveOnto(ontoAtCenterOfLinks.getOntoFilePath())
        }
    }
}

class OntologiesNetworkHandler(private val fixedRateTimer: Timer) {

    fun stop() {
        fixedRateTimer.cancel()
    }
}