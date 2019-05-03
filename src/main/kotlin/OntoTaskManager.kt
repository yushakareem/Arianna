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
    init {

        userObservable = BehaviorSubject.createDefault(isDoingActivity)
        taskObservable = BehaviorSubject.createDefault(drHasActivationState)

        userObservable
                .distinctUntilChanged()
                .subscribeBy(
                        onNext = { doingActivity(it) },
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
        /** Push all the Data Statements in the ontology  */

        // Statements which go to Ontology
        listOfStatements.forEach {
            onto.addOrUpdateToOnto(it)
        }
        // Update Ontology with CurrentTimestamp and Synchronize reasoner
        syncTimeToOnto("Instant_CurrentTimeStamp")

        // Save the Ontology
        onto.saveOnto(onto.getOntoFilePath())
    }

    fun pushToOntoObject(vararg listOfStatements: ObjectPropertyStatement) = pushToOntoObject(listOfStatements.asList())
    fun pushToOntoObject(listOfStatements: List<ObjectPropertyStatement>) {
        /** Push all the Object Statements in the ontology  */

        // Pushing each statement into the Ontology
        listOfStatements.forEach {
            onto.addOrUpdateToOnto(it,true)
        }

        // Update Ontology with CurrentTimestamp and Synchronize reasoner
        syncTimeToOnto("Instant_CurrentTimeStamp")

        // Save the Ontology
        onto.saveOnto(onto.getOntoFilePath())
    }

    fun pullAndManageOnto(userNode: String) { //getData()
        /** Read latest inferences from the ontology */
        println("pullAndManageOnto")

        // Catch the user isDoingActivity statement and observe it
        val sia = IncompleteStatement(userNode, "isDoingActivity")
        isDoingActivity = onto.inferFromOntoToReturnOPStatement(sia)
        userObservable.onNext(isDoingActivity)

        // Catch the drugReminder state statement and observe it
        val sib = IncompleteStatement("DrugReminder", "hasActivationState")
        drHasActivationState = onto.inferFromOntoToReturnOPStatement(sib)
        taskObservable.onNext(drHasActivationState)
    }


    private fun doingActivity(opStatement: ObjectPropertyStatement) {
        /** Management of a user's activities */

        if(opStatement.objectAsOwlIndividual == "HavingBreakfast") {
            println(">>>>> HavingBreakfast detected")

            // Acquisition of data relating to the user to know if he/she has to take medicine
            val medicineToTake = fbDBConnector.checkDrugUser(opStatement.subjectAsOwlIndividual, "state")

            // Acquisition of DrugReminder state from Ontology
            val si1 = IncompleteStatement("DrugReminder", "hasActivationState")
            val sop1 = onto.inferFromOntoToReturnOPStatement(si1)

            // Acquisition of DrugReminder status from Ontology
            val drIncStatus = IncompleteStatement(opStatement.subjectAsOwlIndividual, "drugReminderStatus")
            val drOPStatus = onto.inferFromOntoToReturnOPStatement(drIncStatus)

            println(">>>>>DR: First DrugReminder   =>  (medicineToTake != false && DrugReminderState != true && DrugReminderSatus != 'idle') ?? "+(medicineToTake != "False" && sop1.objectAsOwlIndividual != "True" && drOPStatus.objectAsOwlIndividual == "idle"))
            if(medicineToTake != "False" && sop1.objectAsOwlIndividual != "True" && drOPStatus.objectAsOwlIndividual == "idle"){
                println(">>>>>DR: activated")

                /** Activation and initialization of Drug reminder Task */
                // Update Drug Reminder's state on the FirebaseDB as TRUE
                fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/drugReminderFullStomach", true)

                // Counter acquisition from Firestore
                val medicineCounter = fbDBConnector.checkDrugUser(opStatement.subjectAsOwlIndividual, "counter") as Long
                // Save the counter acquired also in the Ontology
                val dpStatement1 = DataPropertyStatement("DrugReminderConfirmation", "hasCounter", medicineCounter)
                pushToOntoData(dpStatement1)

                // Update the DrugReminderConfirmation with the current time
                //that is the last time the voice interface asked the user
                syncTimeToOnto("DrugReminderConfirmation")

                //Call the timer function to pushToOntoData the current Time and menage the inference result
                thread(start = true) {
                    // Catch the timeElapsedMinute from the ontology to use it for the time
                    val si2 = IncompleteStatement("DrugReminderConfirmation", "timeElapsedMinute")
                    val sop2 = onto.inferFromOntoToReturnDPStatement(si2)
                    val number = sop2.objectAsAnyData as Double

                    println(">>>>>DR: waitint ...")
                    Thread.sleep(number.roundToLong() * 60000) // Minutes
                    println(">>>>>DR: Finished to wait")

                    // Update Ontology with CurrentTimestamp and Synchronize reasoner
                    reasonWithSynchedTime("Instant_CurrentTimeStamp")

                    // Save the new Drug Reminder's state also in Ontology
                    val opStatement1 = ObjectPropertyStatement("DrugReminder", "hasActivationState", "True")
                    pushToOntoObject(opStatement1)

                    // Save the Ontology
                    onto.saveOnto(onto.getOntoFilePath())

                    // Sync and menage the ontology results
                    pullAndManageOnto(opStatement.subjectAsOwlIndividual)
                }
            }
        }
    }

    private fun drugReminder(opStatement: ObjectPropertyStatement) {
        println("Task drug reminder management")
        /** Task drug reminder management */

        // Acquisition of DrugReminderConfirmation state from Ontology
        val drIncState = IncompleteStatement("DrugReminder", "hasActivationState")
        val drOPState = onto.inferFromOntoToReturnOPStatement(drIncState)

        // Acquisition of DrugReminderConfirmation counter from Ontology
        val drIncCounter = IncompleteStatement("DrugReminderConfirmation", "hasCounter")
        val drDPCounter = onto.inferFromOntoToReturnDPStatement(drIncCounter)

        // Acquisition of DrugReminderConfirmation status from Ontology
        val drIncStatus = IncompleteStatement("5fe6b3ba-2767-4669-ae69-6fdc402e695e", "drugReminderStatus")
        val drOPStatus = onto.inferFromOntoToReturnOPStatement(drIncStatus)

        println(">>>>>DR: Ask for Confirmation    =>  (DrugReminder == true && counter > 0 && status != succeed) ?? "+( drOPState.objectAsOwlIndividual == "True" && drDPCounter.objectAsAnyData as Double > 0 && drOPStatus.objectAsOwlIndividual != "succeed"))
        if(drOPState.objectAsOwlIndividual == "True" && drDPCounter.objectAsAnyData as Double > 0 && drOPStatus.objectAsOwlIndividual != "succeed") {
            /** Ask for Confirmation */
            println("+++++DR: Counter = "+drDPCounter)

            // Trigger DrugReminderConfirmation in the vocal Interface writing in FirebaseDB
            fbDBConnector.writeDB("5fe6b3ba-2767-4669-ae69-6fdc402e695e/events/confirmMedicineTaken", true) // ACTIVATES! VocalInterface
            fbDBConnector.writeDB("5fe6b3ba-2767-4669-ae69-6fdc402e695e/events/confirmMedicineTaken", false) // like a switch

            // Update the DrugReminderConfirmation with the current time
            //that is the last time the voice interface asked the user
            syncTimeToOnto("DrugReminderConfirmation")

            // Decrease DrugReminderConfirmation's counter
            val currentCounterValue = drDPCounter.objectAsAnyData as Double
            val newCounterValue = currentCounterValue - 1

            // Update  DrugReminderConfirmation's counter in the Ontology
            val dpStatementCount = DataPropertyStatement("DrugReminderConfirmation", "hasCounter", newCounterValue.toLong())
            pushToOntoData(dpStatementCount)

            //Call the timer function to pushToOntoData the current Time and menage the inference result
            thread(start = true) {
                val si2 = IncompleteStatement("DrugReminderConfirmation", "timeElapsedMinute")
                val sop2 = onto.inferFromOntoToReturnDPStatement(si2)
                val number = sop2.objectAsAnyData as Double

                println(">>>>>DR: waitint ...")
                Thread.sleep(number.roundToLong() * 60000) // Minutes
                println(">>>>>DR: Finished to wait")

                // Update Ontology with CurrentTimestamp and Synchronize reasoner
                reasonWithSynchedTime("Instant_CurrentTimeStamp")

                // Save the Ontology
                onto.saveOnto(onto.getOntoFilePath())

                // Sync and menage the ontology results
                pullAndManageOnto("5fe6b3ba-2767-4669-ae69-6fdc402e695e")
            }

        } else if (drDPCounter.objectAsAnyData as Double <= 0 && drOPStatus.objectAsOwlIndividual != "succeed") {
            /** Drug Reminder Task FAILED */
            println(">>>>>DR: Failed       counter <= 0 && status != succeed  ?" + (drDPCounter.objectAsAnyData as Double <= 0 && drOPStatus.objectAsOwlIndividual != "succeed"))

            // Acquisition of DrugReminderConfirmation state from Ontology
            val statmentActivationState = ObjectPropertyStatement("DrugReminder", "hasActivationState", "False")
            pushToOntoObject(statmentActivationState)

            // Deactivate the Drug Reminder's state, and updateing it on the FirebaseDB as FALSE
            fbDBConnector.writeDB("5fe6b3ba-2767-4669-ae69-6fdc402e695e/events/drugReminderFullStomach", false) // DeACTIVATES! VocalInterface

            // Update the Drug Reminder's status on the FirebaseDB as FAILED
            fbDBConnector.writeDB("5fe6b3ba-2767-4669-ae69-6fdc402e695e/events/drugReminderStatus", "failed ${Timestamp(System.currentTimeMillis())}") // DeACTIVATES! VocalInterface
        }
    }

    fun reasonWithSynchedTime(currentTimeIndividual: String) {
        // Push into the ontology the current time
        syncTimeToOnto(currentTimeIndividual)

        // Start the Ontology's reasoning
        onto.synchronizeReasoner()
    }

    private fun syncTimeToOnto(currentTimeIndividual: String) {
        // Push into the ontology the current time
        val statement1 = DataPropertyStatement(currentTimeIndividual,"hour", Date().hours).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
        val statement2 = DataPropertyStatement(currentTimeIndividual,"minute", Date().minutes).assignSpecialOntoRef(onto.getOntoRef(),onto.getTemporalOntoRef(),onto.getOntoRef())
        onto.addOrUpdateToOnto(statement1)
        onto.addOrUpdateToOnto(statement2)
    }

    fun locationMapper(string: String): String{
        /** Mapper to distinguish the user's location */

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
        /** Mapper to distinguish the DrugReminder status */

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