import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.sql.Timestamp
import java.util.*

import kotlin.concurrent.thread

import kotlin.math.roundToLong

class OntoTaskManager(val onto: Ontology, private val fbDBConnector: FirebaseConnector) {

    @Volatile var isDoingActivity = ObjectPropertyStatement("null","null","null")
    @Volatile var drHasActivationState= ObjectPropertyStatement("null","null","null")
    private var userObservable: BehaviorSubject<ObjectPropertyStatement>
    private var taskObservable: BehaviorSubject<ObjectPropertyStatement>
    var anObservable: BehaviorSubject<String> = BehaviorSubject.createDefault("null")
    init {

        userObservable = BehaviorSubject.createDefault(isDoingActivity)
        taskObservable = BehaviorSubject.createDefault(drHasActivationState)
      //  anObservable = BehaviorSubject.createDefault("null")

        userObservable
                .distinctUntilChanged()
                .subscribeBy(
                        onNext = {
                            doingActivity(it) },
                        onError = { println("onError of doingActivity") }
                )

        taskObservable
                .filter { drHasActivationState.objectAsOwlIndividual == "True" }
                .subscribeBy(

                        onNext = { drugReminder(it) },
                        onError = { println("onError of  drugReminder") }
                )
    }

    fun pushToOntoData(vararg listOfStatements: DataPropertyStatement) = pushToOntoData(listOfStatements.asList())
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

    fun pushToOntoObject(vararg listOfStatements: ObjectPropertyStatement) = pushToOntoObject(listOfStatements.asList())
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

    fun pullAndManageOnto(userNode: String) { //getData()

        // Read latest inferences from the ontology
        val sia = IncompleteStatement(userNode, "isDoingActivity")
        isDoingActivity = onto.inferFromOntoToReturnOPStatement(sia)

        val sib = IncompleteStatement("DrugReminder", "hasActivationState")
        drHasActivationState = onto.inferFromOntoToReturnOPStatement(sib)

        userObservable.onNext(isDoingActivity)
        taskObservable.onNext(isDoingActivity)
    }

    fun letsTry(opStatement: ObjectPropertyStatement) { //REMEMBER

        anObservable
                .filter { it != "null" }
                .subscribeBy(
                        onNext = { computation(it,opStatement) },
                        onError = { println("Unable to read data from firebase!!") },
                        onComplete = { println("SUCCESS!!") }
                )


    }



    // Problem!! Exist only one individual DrugReminder for any user
    private fun doingActivity(opStatement: ObjectPropertyStatement) {

        println("doingActivity Function STARTED")
        letsTry(opStatement)
        fbDBConnector.readDB(opStatement.subjectAsOwlIndividual + "/events/drugReminderStatus", anObservable)
        println("doingActivity Function ENDED")
    }

    fun computation(drStatus:String, opStatement: ObjectPropertyStatement) {

        println("Entered into computation")
        if(opStatement.objectAsOwlIndividual == "HavingBreakfast") {

            println("Entered into computation's IF()")
            val medicineTaken = fbDBConnector.checkDrugUser(opStatement.subjectAsOwlIndividual, "state")
            val si1 = IncompleteStatement("DrugReminder", "hasActivationState")
            val sop1 = onto.inferFromOntoToReturnOPStatement(si1)

            if(medicineTaken != "False" && sop1.objectAsOwlIndividual != "True" && drStatus.contentEquals("idle")){ //Check carefully for the type

                fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/drugReminderFullStomach", true) // ACTIVATES! VocalInterface
                val medicineCounter = fbDBConnector.checkDrugUser(opStatement.subjectAsOwlIndividual, "counter") as Long
                val opStatement1 = ObjectPropertyStatement("DrugReminder", "hasActivationState", "True")
                pushToOntoObject(opStatement1)
                syncTimeToOnto("DrugReminderConfirmation")
                val dpStatement1 = DataPropertyStatement("DrugReminderConfirmation", "hasCounter", medicineCounter)
                pushToOntoData(dpStatement1)
                //Call the timer function to pushToOntoData the current Time and menage the inference result
                thread(start = true) {

                    val si2 = IncompleteStatement("DrugReminderConfirmation", "timeElapsedMinute")
                    val sop2 = onto.inferFromOntoToReturnDPStatement(si2)
                    val number = sop2.objectAsAnyData as Double
                    println("========${Date()}")
                    Thread.sleep(number.roundToLong() * 60000) // Minutes
                    println("========${Date()}")
                    reasonWithSynchedTime("Instant_CurrentTimeStamp")
                    onto.saveOnto(onto.getOntoFilePath())
                    pullAndManageOnto(opStatement.subjectAsOwlIndividual)
                }
            }

        }
    }

    private fun drugReminder(opStatement: ObjectPropertyStatement) {
        println("drugReminder Function STARTED")

        val drIncState = IncompleteStatement("DrugReminderConfirmation", "hasActivationState")
        val drOPState = onto.inferFromOntoToReturnOPStatement(drIncState)
        val drIncCounter = IncompleteStatement("DrugReminderConfirmation", "hasCounter")
        val drDPCounter = onto.inferFromOntoToReturnDPStatement(drIncCounter)
        val drIncStatus = IncompleteStatement(opStatement.subjectAsOwlIndividual, "drugReminderStatus")
        val drOPStatus = onto.inferFromOntoToReturnOPStatement(drIncStatus)

        if(drOPState.objectAsOwlIndividual == "True" && drDPCounter.objectAsAnyData as Double > 0 && drOPStatus.objectAsOwlIndividual != "succeed") {

            fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/confirmMedicineTaken", true) // ACTIVATES! VocalInterface
            fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/confirmMedicineTaken", false) // like a switch

            syncTimeToOnto("DrugReminderConfirmation")

            val currentCounterValue = drDPCounter.objectAsAnyData as Double
            val newCounterValue = currentCounterValue - 1

            val dpStatementCount = DataPropertyStatement("DrugReminderConfirmation", "hasCounter", newCounterValue.toLong())
            pushToOntoData(dpStatementCount)

            thread(start = true) {

                val incTimeElapsed = IncompleteStatement("DrugReminderConfirmation", "timeElapsedMinute")
                val timeElapsed = onto.inferFromOntoToReturnDPStatement(incTimeElapsed)
                val number = timeElapsed.objectAsAnyData as Double

                println("========${Date()}")
                Thread.sleep(number.roundToLong() * 60000) // Minutes
                println("========${Date()}")
                reasonWithSynchedTime("Instant_CurrentTimeStamp")
                // Save the Ontology
                onto.saveOnto(onto.getOntoFilePath())
                pullAndManageOnto(opStatement.subjectAsOwlIndividual)
            }
        } else if (drDPCounter.objectAsAnyData as Double <= 0 && drOPStatus.objectAsOwlIndividual != "succeed") {
            // Update Onto
            val opStatementFalsify = ObjectPropertyStatement("DrugReminder", "hasActivationState", "False")
            pushToOntoObject(opStatementFalsify)
            // FBDB
            fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/drugReminderFullStomach", false) // DeACTIVATES! VocalInterface
            fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/drugReminderStatus", "failed ${Timestamp(System.currentTimeMillis())}") // DeACTIVATES! VocalInterface
        }

        println("drugReminder Function ENDED")
    }

    private fun reasonWithSynchedTime(currentTimeIndividual: String) {

        syncTimeToOnto(currentTimeIndividual)
        onto.synchronizeReasoner()
    }

    private fun syncTimeToOnto(currentTimeIndividual: String) {

        val statement1 = DataPropertyStatement(currentTimeIndividual,"hour", Date().hours).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
        val statement2 = DataPropertyStatement(currentTimeIndividual,"minute", Date().minutes).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
        onto.addOrUpdateToOnto(statement1)
        onto.addOrUpdateToOnto(statement2)
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
    fun statusMapper(string: String): String{

        lateinit var sensorValue: String
        when {
            string.contains("idle") -> sensorValue = "idle"
            string.contains("failed") -> sensorValue = "failed"
            string.contains("active") -> sensorValue = "active"
            string.contains("succeed") -> sensorValue = "succeed"
        }

        return sensorValue
    }
}