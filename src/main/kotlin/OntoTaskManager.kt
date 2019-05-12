import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

import kotlin.concurrent.thread

import kotlin.math.roundToLong

class OntoTaskManager(val onto: Ontology, private val fbDBConnector: FirebaseConnector) {

    @Volatile var isDoingActivity = DataPropertyStatement("null","null","null")
    @Volatile var drHasActivationState= DataPropertyStatement("null","null","null")
    private var userObservable: BehaviorSubject<DataPropertyStatement>
    private var taskObservable: BehaviorSubject<DataPropertyStatement>
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
                .filter { drHasActivationState.objectAsAnyData == true }
                .subscribeBy(
                        onNext = { drugReminder(it)},
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
        reasonWithSynchedTime("Instant_CurrentTime")

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
        reasonWithSynchedTime("Instant_CurrentTime")

        // Save the Ontology
        onto.saveOnto(onto.getOntoFilePath())
    }

    fun pullAndManageOnto(userNode: String) { //getData()
        /** Read latest inferences from the ontology */
        println("pullAndManageOnto")

        // Catch the user isDoingActivity statement and observe it
        var sia = IncompleteStatement(userNode,"isDoingActivity")
        isDoingActivity = onto.inferFromOntoToReturnDPStatement(sia)
        userObservable.onNext(isDoingActivity)

        // Catch the drugReminder state statement and observe it
        val sib = IncompleteStatement(userNode, "isActiveDrugReminder")
        drHasActivationState = onto.inferFromOntoToReturnDPStatement(sib)
        //println(drHasActivationState)
        taskObservable.onNext(drHasActivationState)
    }


    private fun doingActivity(dpStatement: DataPropertyStatement) {
        /** Management of a user's activities */

        if(dpStatement.objectAsAnyData == "HavingBreakfast") {
            println(">>>>> HavingBreakfast detected")

            // Acquisition of data relating to the user to know if he/she has to take medicine
            val medicineToTake = fbDBConnector.checkDrugUser(dpStatement.subject, "state")

            // Acquisition of DrugReminder state from Ontology
            val drIncActive = IncompleteStatement(dpStatement.subject, "isActiveDrugReminder")
            val drActive = onto.inferFromOntoToReturnDPStatement(drIncActive)

            // Acquisition of DrugReminder status from Ontology
            val drIncStatus = IncompleteStatement(dpStatement.subject, "hasCurrentStatusDrugReminder")
            val drOPStatus = onto.inferFromOntoToReturnDPStatement(drIncStatus)

            println(">>>>>DR: First DrugReminder   =>  (medicineToTake != false && DrugReminderState != true && DrugReminderSatus == 'idle') ?? "+(medicineToTake as Boolean && drActive.objectAsAnyData != true && drOPStatus.objectAsAnyData == "idle"))
            if(medicineToTake && drActive.objectAsAnyData != true && drOPStatus.objectAsAnyData == "idle"){
                println(">>>>>DR: activated")

                /** Activation and initialization of Drug reminder Task */
                // Update Drug Reminder's state on the FirebaseDB as TRUE
                fbDBConnector.writeDB(dpStatement.subject+"/events/drugReminderFullStomach", true)

                // Counter acquisition from Firestore
                val medicineCounter = (fbDBConnector.checkDrugUser(dpStatement.subject, "counter") as Long).toDouble()
                // Save the counter acquired also in the Ontology
                val drCounter = DataPropertyStatement(dpStatement.subject, "hasCounterDrugReminder", medicineCounter)
                onto.addOrUpdateToOnto(drCounter)
                onto.saveOnto(onto.getOntoFilePath())

                // Update the DrugReminderConfirmation with the current time
                //that is the last time the voice interface asked the user
                val drLastTime = DataPropertyStatement(dpStatement.subject,"hasTimeDrugReminder", getTime())
                onto.addOrUpdateToOnto(drLastTime)

                val timeElapse = fbDBConnector.checkTimeElapse(dpStatement.subject, "high")
                val timeElapseIncStatement = DataPropertyStatement(dpStatement.subject,"hasTimeElapsedDrugReminder", timeElapse.toFloat())
                onto.addOrUpdateToOnto(timeElapseIncStatement)

                //Call the timer function to pushToOntoData the current Time and menage the inference result
                thread(start = true) {
                    println(">>>>>DR: waitint ...")
                    Thread.sleep(timeElapse as Long * 60000) // Minutes
                    println(">>>>>DR: Finished to wait")

                    // Save the new Drug Reminder's state also in Ontology
                    val drActive = DataPropertyStatement(dpStatement.subject, "isActiveDrugReminder", true)
                    pushToOntoData(drActive)

                    // Sync and menage the ontology results
                    pullAndManageOnto(dpStatement.subject)
                }
            }
        }
    }

    private fun drugReminder(opStatement: DataPropertyStatement) {
        println("Task drug reminder management")
        val userId = opStatement.subject
        /** Task drug reminder management */

        // Acquisition of DrugReminderConfirmation state from Ontology
        val drIncState = IncompleteStatement(userId, "isActiveDrugReminder")
        val drDPState = onto.inferFromOntoToReturnDPStatement(drIncState)

        // Acquisition of DrugReminderConfirmation state from Ontology
        val drIncStateConfirmation = IncompleteStatement(userId, "isActiveDrugReminderConfirmation")
        val drDPStateConfirmation = onto.inferFromOntoToReturnDPStatement(drIncStateConfirmation)

        // Acquisition of DrugReminderConfirmation counter from Ontology
        val drIncCounter = IncompleteStatement(userId, "hasCounterDrugReminder")
        val drDPCounter = onto.inferFromOntoToReturnDPStatement(drIncCounter)

        // Acquisition of DrugReminderConfirmation status from Ontology
        val drIncStatus = IncompleteStatement(userId, "hasCurrentStatusDrugReminder")
        val drDPStatus = onto.inferFromOntoToReturnDPStatement(drIncStatus)

        println(drDPStateConfirmation.objectAsAnyData == true)
        println(">>>>>DR: Ask for Confirmation    =>  (DrugReminder == true && DrugReminderConfirmation == true && counter > 0 && status != succeed) ?? "+(drDPStateConfirmation.objectAsAnyData == true &&  drDPState.objectAsAnyData as Boolean && drDPCounter.objectAsAnyData as Float > 0 && drDPStatus.objectAsAnyData != "succeed"))
        if(drDPStateConfirmation.objectAsAnyData == true && drDPState.objectAsAnyData as Boolean && drDPCounter.objectAsAnyData as Float > 0 && drDPStatus.objectAsAnyData != "succeed") {
            /** Ask for Confirmation */

            // Trigger DrugReminderConfirmation in the vocal Interface writing in FirebaseDB
            fbDBConnector.writeDB("$userId/events/confirmMedicineTaken", true) // ACTIVATES! VocalInterface
            fbDBConnector.writeDB("$userId/events/confirmMedicineTaken", false) // like a switch

            // Update the DrugReminderConfirmation with the current time
            //that is the last time the voice interface asked the user
            val drLastTime = DataPropertyStatement(userId,"hasTimeDrugReminder", getTime())
            onto.addOrUpdateToOnto(drLastTime)

            // Decrease DrugReminderConfirmation's counter
            val currentCounterValue = drDPCounter.objectAsAnyData as Float
            val newCounterValue = currentCounterValue - 1

            // Update  DrugReminderConfirmation's counter in the Ontology
            val dpStatementCount = DataPropertyStatement(userId, "hasCounterDrugReminder", newCounterValue)
            onto.addOrUpdateToOnto(dpStatementCount)

            val si2 = IncompleteStatement(userId, "hasTimeElapsedDrugReminder")
            val sop2 = onto.inferFromOntoToReturnDPStatement(si2)
            val number = sop2.objectAsAnyData as Float

//            onto.saveOnto(onto.getOntoFilePath())

            //Call the timer function to pushToOntoData the current Time and menage the inference result
            thread(start = true) {
                println(">>>>>DR confirmation: waitint ...")
                Thread.sleep(number.roundToLong() * 60000) // Minutes
                println(">>>>>DR confirmation: Finished to wait")

                // Update Ontology with CurrentTimestamp and Synchronize reasoner
                reasonWithSynchedTime("Instant_CurrentTime")

                // Save the Ontology
                onto.saveOnto(onto.getOntoFilePath())

                // Sync and menage the ontology results
                pullAndManageOnto("5fe6b3ba-2767-4669-ae69-6fdc402e695e")
            }

        } else if (drDPCounter.objectAsAnyData as Float <= 0 && drDPStatus.objectAsAnyData != "succeed") {
            /** Drug Reminder Task FAILED */
            println(">>>>>DR: Failed       counter <= 0 && status != succeed  ?" + (drDPCounter.objectAsAnyData as Float <= 0 && drDPStatus.objectAsAnyData != "succeed"))

            // Deactivate the Drug Reminder's state, and updateing it on the FirebaseDB as FALSE
            fbDBConnector.writeDB("5fe6b3ba-2767-4669-ae69-6fdc402e695e/events/drugReminderFullStomach", false) // DeACTIVATES! VocalInterface

            // Update the Drug Reminder's status on the FirebaseDB as FAILED
            fbDBConnector.writeDB("5fe6b3ba-2767-4669-ae69-6fdc402e695e/events/drugReminderStatus", "failed ${Timestamp(System.currentTimeMillis())}") // DeACTIVATES! VocalInterface

            //DELETE ALL THE STUFF ABOUT DRUG REMINDER IN THE ONTOLOGY
//            val hasCounterDrugReminder = DataPropertyStatement(userId, "hasCounterDrugReminder", 0.1)
//            onto.breakStatementInOnto(hasCounterDrugReminder)
//
//            val hasTimeElapsedDrugReminder = DataPropertyStatement(userId, "hasTimeElapsedDrugReminder", 0.0)
//            onto.breakStatementInOnto(hasTimeElapsedDrugReminder)
//
//            val hasTimeDrugReminder = DataPropertyStatement(userId, "hasTimeDrugReminder", 0)
//            onto.breakStatementInOnto(hasTimeDrugReminder)
//
//            val isActiveDrugReminder = DataPropertyStatement(userId, "isActiveDrugReminder", true)
//            onto.breakStatementInOnto(isActiveDrugReminder)

            //REMOVAL PROCESS

            Thread.sleep(2000) // With a little bit of wait, getting the correct inference for hasCurrentStatusDrugreminder as "Failed"

            println("\n We are in the new system!")

            val drIncStatus1 = IncompleteStatement(userId, "hasCurrentStatusDrugReminder")
            val drDPStatus1 = onto.inferFromOntoToReturnDPStatement(drIncStatus1)

            println("hasCurrentStatusDrugReminder: ${drDPStatus1.objectAsAnyData as String}")
            println((drDPStatus1.objectAsAnyData as String).contains("Failed", ignoreCase = true))
            if ((drDPStatus1.objectAsAnyData as String).contains("Failed", ignoreCase = true)) {
                val verbList: MutableList<String> = mutableListOf()
                verbList.add("hasCounterDrugReminder")
                verbList.add("hasTimeElapsedDrugReminder")
                verbList.add("hasTimeDrugReminder")
                verbList.add("isActiveDrugReminder")
                onto.addToCleaner(userId,verbList)
            }
                println("\n \n == !! == Reached end of thread == !! ==")

            println("\n \n == !! == Reached end of ElseIf == !! ==")
        }

        println("\n \n == !! == Reached end of drugReminder() == !! ==")
    }

    fun reasonWithSynchedTime(currentTimeIndividual: String) {
        // Push into the ontology the current time
        syncTimeToOnto(currentTimeIndividual)

        // Start the Ontology's reasoning
        onto.synchronizeReasoner()
    }

    private fun syncTimeToOnto(currentTimeIndividual: String) {
        // Push into the ontology the current time
        val statementTime = DataPropertyStatement(currentTimeIndividual,"hasTime", getTime())
        onto.addOrUpdateToOnto(statementTime)
    }

    fun locationMapper(string: String): String{
        /** Mapper to distinguish the user's location */
        lateinit var sensorValue: String
        when (string) {
            "6" -> sensorValue = "Kitchen"
            "5" -> sensorValue = "LivingRoom"
            "4" -> sensorValue = "BathRoom"
            "3" -> sensorValue = "BedRoom"
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

    fun getTime():Int{
        val cal = Calendar.getInstance()
        return SimpleDateFormat("HH").format(cal.time).toInt()*60+SimpleDateFormat("mm").format(cal.time).toInt()
    }
}