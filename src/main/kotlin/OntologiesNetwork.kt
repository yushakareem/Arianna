/**
 * This class, (i) connects the links between Ontologies in the Network, (ii) allows to start networking, and (iii) returns a handler object that can allow to stop networking.
 *
 * @return OntologiesNetworkHandler
 */

import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.lang.IllegalStateException

class OntologiesNetwork {
//
//    fun startNetworking(vararg ontoLinksConfigList: OntologyLinksConfiguration) = startNetworking(ontoLinksConfigList.asList())
//
//    private fun startNetworking(ontoLinksConfigList: List<OntologyLinksConfiguration>): OntologiesNetworkHandler {
//
//        lateinit var fixedRateTimer: Timer
//
//        ontoLinksConfigList.toObservable()
//                .filter { it.isScheduledAndObservable }
//                .subscribeBy(
//                        onNext = {
//                            fixedRateTimer = fixedRateTimer(name = "${it.ontoAtCenterOfLinks.getOntoRef().referenceName}_ScheduledNonDaemonThread", initialDelay = it.schedulerInitialDelay, period = it.schedulerIntervalPeriod) {
//
//                                val observableInferredStatement = activateOntology(it,false)
//                                observableInferredStatement.subscribe { inferredStatement -> activateObserverOntologies(inferredStatement,ontoLinksConfigList) }
//                            }
//                        },
//                        onError = { it.printStackTrace() },
//                        onComplete = { println("Activated scheduled-observable ontology.") } //dont print, if withAriannaAnalyticsDisabled()
//                )
//
//        return OntologiesNetworkHandler(fixedRateTimer)
//    }
//
//    private fun activateObserverOntologies(inferredStatementFromObservable: ObjectPropertyStatement, ontoLinksConfigList: List<OntologyLinksConfiguration>) {
//
//        ontoLinksConfigList.toObservable()
//                .filter { it.isAnObserver }
////                .filter { inferredStatementFromObservable.compare(it.activationStatementToObserve) }
//                .subscribeBy(
//                        onNext = { activateOntology(it, true) },
//                        onError = { it.printStackTrace() },
//                        onComplete = { println("Checked activation condition of observer ontologies. And activated it, if condition got satisfied.")  } //dont print, if withAriannaAnalyticsDisabled()
//                )
//    }
//
//    private fun activateOntology(ontoLinksConfig: OntologyLinksConfiguration, withAssertTemporalRelations: Boolean): Observable<ObjectPropertyStatement> {
//
//        transferDataFromDBToOnto(ontoLinksConfig.inputDBInfo, ontoLinksConfig.mapOfDBTablesToStatements, ontoLinksConfig.ontoAtCenterOfLinks)
//        if (withAssertTemporalRelations) assertTemporalRelations(ontoLinksConfig)
//        return Observable.just(transferInferencesFromOntoToDB(ontoLinksConfig.ontoAtCenterOfLinks, ontoLinksConfig.mapOfStatementsToDBTables, ontoLinksConfig.outputDBInfo))
//    }
//
//    private fun assertTemporalRelations(ontoLinksConfig: OntologyLinksConfiguration) {
//        // For now incomplete
//    }
//
//    private fun transferInferencesFromOntoToDB(ontoAtCenterOfLinks: Ontology, mapStatementToDBTable: HashMap<IncompleteStatement, String>, outputDBInfo: MySqlConnector): ObjectPropertyStatement {
//
//        var inferredObjectPropertyStatement = ObjectPropertyStatement("null", "null", "null")
//
//        mapStatementToDBTable.iterator().forEach {
//
//            outputDBInfo.connectToDB()
//            try {
//                ontoAtCenterOfLinks.synchronizeReasoner()
//                val inferredObjectStatement = ontoAtCenterOfLinks.inferFromOntoToReturnOPStatement(it.key)
//                outputDBInfo.setStringValue(it.value, Timestamp(System.currentTimeMillis()), "${it.key.getSubject()}_${it.key.getVerb()}_${inferredObjectStatement.getObject()}")
//                inferredObjectPropertyStatement = ObjectPropertyStatement(it.key.getSubject(), it.key.getVerb(), inferredObjectStatement.getObject())
//                println("\nInferred outputHAR-statement in '${ontoAtCenterOfLinks.getOntoRef().referenceName}' is seen above.") //dont print, if withAriannaAnalyticsDisabled()
//            } catch (e: IllegalStateException) {
//                println("\nInference of outputHAR-statement in '${ontoAtCenterOfLinks.getOntoRef().referenceName}' is NULL.") // due to 3 possible reasons: \n(1) Ontology is NOT UPDATED properly with sensor values from DB. \n(2) Due to the design of rules in Ontology. Although Ontology is UPDATED with sensor values from DB. \n(3) Due to miss-typing while building statements on JAVA side. Although Ontology is UPDATED with sensor values from DB.\n")
//                //dont print, if withAriannaAnalyticsDisabled()
//            }
//            outputDBInfo.disconnectFromDB()
//        }
//
//        return inferredObjectPropertyStatement
//    }
//
//    private fun transferDataFromDBToOnto(inputDBInfo: MySqlConnector, mapDBTableToStatement: HashMap<String, IncompleteStatement>, ontoAtCenterOfLinks: Ontology) {
//
//        // iterating over the map of (DBTables -> StatementsInOntology)
//        mapDBTableToStatement.iterator().forEach {
//
//            inputDBInfo.connectToDB()
//
//            val datatype = inputDBInfo.getDatatypeOfTheValue(it.key)
//            val resultSet = inputDBInfo.readLatestRow(it.key)
//
//            if (resultSet.next()) {
//
//                lateinit var sensorValueStatement: DataPropertyStatement
//                when (datatype) {
//                    "varchar" -> {
//                        val sensorValue = inputDBInfo.getStringValue(resultSet)
//                        sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
//                    }
//                    "tinyint" -> {
//                        val sensorValue = inputDBInfo.getBooleanValue(resultSet)
//                        sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
//                    }
//                    "double" -> {
//                        val sensorValue = inputDBInfo.getDoubleValue(resultSet)
//                        sensorValueStatement = DataPropertyStatement(it.value.getSubject(), it.value.getVerb(), sensorValue)
//                    }
//                }
//                val timeStamp = inputDBInfo.getTimestamp(resultSet)
//                val sensorTimestampStatement = DataPropertyStatement(it.value.getSubject(), "hasTimestamp", timeStamp)
//                ontoAtCenterOfLinks.addOrUpdateToOnto(sensorValueStatement)
//                ontoAtCenterOfLinks.addOrUpdateToOnto(sensorTimestampStatement)
//            } else {
//                error("ResultSet is empty. Please check the query for MySQL-DB.")
//            }
//            inputDBInfo.disconnectFromDB()
//            ontoAtCenterOfLinks.saveOnto(ontoAtCenterOfLinks.getOntoFilePath())
//        }
//    }
//
//    /**
//     * Provides an object that can handle a running OntologiesNetwork.
//     * Allows to stop networking.
//     */
//    class OntologiesNetworkHandler(private val fixedRateTimer: Timer) {
//
//        fun stopNetworking() {
//            fixedRateTimer.cancel()
//        }
//    }
}
