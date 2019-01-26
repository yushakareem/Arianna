import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

class OntologiesNetwork {

    fun startNetworking(vararg ontoLinksConfig: OntologyLinksConfiguration) = startNetworking(ontoLinksConfig.asList())

    private fun startNetworking(ontoLinksConfig: List<OntologyLinksConfiguration>): OntologiesNetworkHandler {

        lateinit var fixedRateTimer: Timer

        // iterating over linksConfiguration of all Ontologies.
        ontoLinksConfig.listIterator().forEach {

            if (it.isActivatedByScheduler) {

                fixedRateTimer = fixedRateTimer(name = "PeriodicNonDaemonThread", initialDelay = it.schedulerInitialDelay, period = it.schedulerIntervalPeriod) {

                    transferDataFromDBToOnto(it.inputDBInfo, it.mapOfDBTablesToStatements, it.ontoAtCenterOfLinks)
                    transferInferencesFromOntoToDB(it.ontoAtCenterOfLinks, it.mapOfStatementsToDBTables, it.outputDBInfo)
                }
            } else if (it.isActivatedByOntology) {

                println("Here will be the iteration over ontologies expecting to be activated by another ontology")
            } else {

                error("Links of the Ontologies were not built properly. Please check.")
            }
        }

        return OntologiesNetworkHandler(fixedRateTimer)
    }

    private fun transferInferencesFromOntoToDB(ontoAtCenterOfLinks: Ontology, mapStatementToDBTable: HashMap<IncompleteStatement, String>, outputDBInfo: MySqlConnector) {

        mapStatementToDBTable.iterator().forEach {

            outputDBInfo.connectToDBorCreateNewDB()
            try {
                ontoAtCenterOfLinks.synchronizeReasoner()
                val inferredObjectStatement = ontoAtCenterOfLinks.inferFromOntoToReturnOPStatement(it.key)
                outputDBInfo.setStringValue(it.value, Timestamp(System.currentTimeMillis()), "${it.key.getSubject()}_${it.key.getVerb()}_${inferredObjectStatement.getObject()}")
            } catch (e: IllegalStateException) {
                println("\nInference is NULL due to 3 possible reasons: \n(1) Ontology is NOT UPDATED properly with sensor values from DB. \n(2) Due to the design of rules in Ontology. Although Ontology is UPDATED with sensor values from DB. \n(3) Due to miss-typing while building statements on JAVA side. Although Ontology is UPDATED with sensor values from DB.\n")
            }
            outputDBInfo.disconnectFromDB()
        }
    }

    private fun transferDataFromDBToOnto(inputDBInfo: MySqlConnector, mapDBTableToStatement: HashMap<String, IncompleteStatement>, ontoAtCenterOfLinks: Ontology) {

        // iterating over the map of (DBTables -> StatementsInOntology)
        mapDBTableToStatement.iterator().forEach {

            inputDBInfo.connectToDBorCreateNewDB()
            val resultSet = inputDBInfo.readLatestRow(it.key)
            if (resultSet.next()) {
                val sensorValue = inputDBInfo.getStringValue(resultSet)
                val timeStamp = inputDBInfo.getTimestamp(resultSet)
                val sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
                val sensorTimestampStatement = DataPropertyStatement(it.value.getSubject(), "hasTimestamp", timeStamp)
                ontoAtCenterOfLinks.addOrUpdateToOnto(sensorValueStatement)
                ontoAtCenterOfLinks.addOrUpdateToOnto(sensorTimestampStatement)
            }
            inputDBInfo.disconnectFromDB()
            ontoAtCenterOfLinks.saveOnto(ontoAtCenterOfLinks.getOntoFilePath())
        }
    }

    class OntologiesNetworkHandler(private val fixedRateTimer: Timer) {

        fun stop() {
            fixedRateTimer.cancel()
        }
    }
}