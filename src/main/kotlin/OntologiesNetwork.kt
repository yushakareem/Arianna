import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.lang.IllegalStateException


class OntologiesNetwork {

    fun startNetworking(vararg ontoLinksConfigList: OntologyLinksConfiguration) = startNetworking(ontoLinksConfigList.asList())

    private fun startNetworking(ontoLinksConfigList: List<OntologyLinksConfiguration>): OntologiesNetworkHandler {

        lateinit var fixedRateTimer: Timer

        ontoLinksConfigList.toObservable()
                .filter { it.isActivatedByScheduler }
                .subscribeBy(
                        onNext = {
                            fixedRateTimer = fixedRateTimer(name = "${it.ontoAtCenterOfLinks.getOntoRef().referenceName}_ScheduledNonDaemonThread", initialDelay = it.schedulerInitialDelay, period = it.schedulerIntervalPeriod) {

                                transferDataFromDBToOnto(it.inputDBInfo, it.mapOfDBTablesToStatements, it.ontoAtCenterOfLinks)
                                val observableOntoStatement = Observable.just(transferInferencesFromOntoToDB(it.ontoAtCenterOfLinks, it.mapOfStatementsToDBTables, it.outputDBInfo))
                                observableOntoStatement.subscribe { inferredStatement -> observerOntologies(inferredStatement,ontoLinksConfigList) }
                            }
                        },
                        onError = { it.printStackTrace() },
                        onComplete = { println("Completed: activation of scheduled thread.") }
                )

        return OntologiesNetworkHandler(fixedRateTimer)
    }

    private fun observerOntologies(inferredStatementFromObservable: ObjectPropertyStatement, ontoLinksConfigList: List<OntologyLinksConfiguration>) {

        ontoLinksConfigList.toObservable()
                .filter { it.isActivatedByOntology }
                .subscribeBy(
                        onNext = {
                            if (inferredStatementFromObservable.compare(it.activationStatement)) {
//                                println("Activating: ${it.ontoAtCenterOfLinks.getOntoRef().referenceName} ontology.")
                                activateOntology(it)
                            }
                            else println("Checking activation condition of ${it.ontoAtCenterOfLinks.getOntoRef().referenceName} ontology, next.")
                        },
                        onError = { it.printStackTrace() },
                        onComplete = { println("Completed: Activated ontologies by checking their activation condition.") }
                )
    }

    private fun activateOntology(ontoLinksConfig: OntologyLinksConfiguration) {
        transferDataFromDBToOnto(ontoLinksConfig.inputDBInfo, ontoLinksConfig.mapOfDBTablesToStatements, ontoLinksConfig.ontoAtCenterOfLinks)
//        assertTemporalRelations(ontoLinksConfig)
//        transferInferencesFromOntoToDB()
    }

    private fun assertTemporalRelations(ontoLinksConfig: OntologyLinksConfiguration) {

    }

    private fun transferInferencesFromOntoToDB(ontoAtCenterOfLinks: Ontology, mapStatementToDBTable: HashMap<IncompleteStatement, String>, outputDBInfo: MySqlConnector): ObjectPropertyStatement {

        var inferredObjectPropertyStatement = ObjectPropertyStatement("null", "null", "null")

        mapStatementToDBTable.iterator().forEach {

            outputDBInfo.connectToDBorCreateNewDB()
            try {
                ontoAtCenterOfLinks.synchronizeReasoner()
                val inferredObjectStatement = ontoAtCenterOfLinks.inferFromOntoToReturnOPStatement(it.key)
                outputDBInfo.setStringValue(it.value, Timestamp(System.currentTimeMillis()), "${it.key.getSubject()}_${it.key.getVerb()}_${inferredObjectStatement.getObject()}")
                inferredObjectPropertyStatement = ObjectPropertyStatement(it.key.getSubject(), it.key.getVerb(), inferredObjectStatement.getObject())
            } catch (e: IllegalStateException) {
                println("\nInference is NULL")// due to 3 possible reasons: \n(1) Ontology is NOT UPDATED properly with sensor values from DB. \n(2) Due to the design of rules in Ontology. Although Ontology is UPDATED with sensor values from DB. \n(3) Due to miss-typing while building statements on JAVA side. Although Ontology is UPDATED with sensor values from DB.\n")
            }
            outputDBInfo.disconnectFromDB()
        }

        return inferredObjectPropertyStatement
    }

    private fun transferDataFromDBToOnto(inputDBInfo: MySqlConnector, mapDBTableToStatement: HashMap<String, IncompleteStatement>, ontoAtCenterOfLinks: Ontology) {

        // iterating over the map of (DBTables -> StatementsInOntology)
        mapDBTableToStatement.iterator().forEach {

            inputDBInfo.connectToDBorCreateNewDB()

            val datatype = inputDBInfo.getDatatypeOfTheValue(it.key)
            val resultSet = inputDBInfo.readLatestRow(it.key)

            if (resultSet.next()) {

                lateinit var sensorValueStatement: DataPropertyStatement
                when (datatype) {
                    "varchar" -> {
                        val sensorValue = inputDBInfo.getStringValue(resultSet)
                        sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
                    }
                    "tinyint" -> {
                        val sensorValue = inputDBInfo.getBooleanValue(resultSet)
                        sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
                    }
                    "double" -> {
                        val sensorValue = inputDBInfo.getDoubleValue(resultSet)
                        sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
                    }
                }
                val timeStamp = inputDBInfo.getTimestamp(resultSet)
                val sensorTimestampStatement = DataPropertyStatement(it.value.getSubject(), "hasTimestamp", timeStamp)
                ontoAtCenterOfLinks.addOrUpdateToOnto(sensorValueStatement)
                ontoAtCenterOfLinks.addOrUpdateToOnto(sensorTimestampStatement)
            } else {
                error("ResultSet is empty. Please check the query for MySQL-DB.")
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
