import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

import java.util.*
import kotlin.concurrent.thread
import kotlin.math.roundToLong

class OntoTaskManager(private val onto: Ontology, private val fbDBConnector: FirebaseConnector) {

    fun pushToOntoData(listOfStatements: List<DataPropertyStatement>) {
        // Statements which go to Ontology
        listOfStatements.forEach {
            onto.addOrUpdateToOnto(it)
        }
        // Update Ontology with CurrentTimestamp and Synchronize reasoner
        reasonWithSynchedTime("Instant_CurrentTimeStamp")

        // Save the Ontology
        onto.saveOnto(onto.getOntoFilePath())
    }
    fun pushToOntoObject(listOfStatements: List<ObjectPropertyStatement>) {
        // Statements which go to Ontology
        listOfStatements.forEach {
            onto.addOrUpdateToOnto(it,true)
        }
        // Update Ontology with CurrentTimestamp and Synchronize reasoner
        reasonWithSynchedTime("Instant_CurrentTimeStamp")

        // Save the Ontology
        onto.saveOnto(onto.getOntoFilePath())
    }


    fun pullAndManageOnto(userNode: String) {

        // Read latest inferences from the ontology
        val si1 = IncompleteStatement(userNode, "isDoingActivity")
        val sop1 = onto.inferFromOntoToReturnOPStatement(si1)

        val si2 = IncompleteStatement("DrugReminder", "hasActivationState")
        val sop2 = onto.inferFromOntoToReturnOPStatement(si2)

        // An Observable of the inference
        val userObservable = Observable.just(sop1)
        userObservable
                .subscribeOn(Schedulers.io())
                .subscribeBy(

                        onNext = { doingActivity(it) },
                        onError = { println("Some code runs if some error in doingActivity") }
                )

        val taskObservable = Observable.just(sop2)
        taskObservable
                .subscribeOn(Schedulers.io())
                .filter { sop2.getObject() == "True" }
                .subscribeBy(

                        onNext = { drugReminder(it) },
                        onError = { println("Some code runs if some error in Drugreminder hasActivation") }
                )
    }

    // Problem!! Exist only one individual DrugReminder for any user
    private fun doingActivity(opStatement: ObjectPropertyStatement) {

        if(opStatement.getObject() == "HavingBreakfast") {

            val medicineTaken = fbDBConnector.checkDrugUser(opStatement.getSubject()) // True False Null

            val si1 = IncompleteStatement("DrugReminder", "hasActivationState")
            val sop1 = onto.inferFromOntoToReturnOPStatement(si1)

            if(medicineTaken !== null && sop1.getObject() !== "True"){
                fbDBConnector.writeDB(opStatement.getSubject()+"/events/drugReminderFullStomach", true) // ACTIVATES! VocalInterface

                val opStatement1 = ObjectPropertyStatement("DrugReminder", "hasActivationState", "True")
                val statementObjectList : List<ObjectPropertyStatement> = listOf(opStatement1)
                pushToOntoObject(statementObjectList)

                val dpStatement1 = DataPropertyStatement("DrugReminderConfirmation", "hour", Date().hours).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
                val dpStatement2 = DataPropertyStatement("DrugReminderConfirmation", "minute", Date().minutes).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
                val statementDataList : List<DataPropertyStatement> = listOf(dpStatement1,dpStatement2)
                pushToOntoData(statementDataList)

                //Call the timer function to pushToOntoData the current Time and menage the inference result
                thread(start = true){
                    val si2 = IncompleteStatement("DrugReminderConfirmation","timeElapsedMinute")
                    val sop2 = onto.inferFromOntoToReturnDPStatement(si2)
                    println(sop2.getObjectAnyData())

                    val number = sop2.getObjectAnyData() as Double

                    Thread.sleep(number.roundToLong()*1000) // 5 seconds for now
                    println("DONE!!")
                }
            }
        }
    }

    private fun drugReminder(opStatement: ObjectPropertyStatement) {
        println("drugReminder Function EXECUTED")
    }

    private fun reasonWithSynchedTime(currentTimeIndividual: String) {
        val statement1 = DataPropertyStatement(currentTimeIndividual,"hour", Date().hours).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
        val statement2 = DataPropertyStatement(currentTimeIndividual,"minute", Date().minutes).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
        onto.addOrUpdateToOnto(statement1)
        onto.addOrUpdateToOnto(statement2)
        onto.synchronizeReasoner()
    }

    fun locationMapper(string: String): String{

        lateinit var sensorValue: String
        when (string) {
            "6" -> sensorValue = "Kitchen"
            "2" -> sensorValue = "LivingRoom"
            "3" -> sensorValue = "BathRoom"
            "4" -> sensorValue = "BedRoom"
        }
        return sensorValue
    }
}