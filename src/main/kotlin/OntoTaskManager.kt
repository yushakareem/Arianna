import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject

import java.util.*
import kotlin.concurrent.thread
import kotlin.math.roundToLong

class OntoTaskManager(private val onto: Ontology, private val fbDBConnector: FirebaseConnector) {

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

//    fun startNetworking(vararg ontoLinksConfigList: OntologyLinksConfiguration) = startNetworking(ontoLinksConfigList.asList())

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


    // Problem!! Exist only one individual DrugReminder for any user
    private fun doingActivity(opStatement: ObjectPropertyStatement) {

        if(opStatement.objectAsOwlIndividual == "HavingBreakfast") {

            val medicineTaken = fbDBConnector.checkDrugUser(opStatement.subjectAsOwlIndividual, "state")
            val si1 = IncompleteStatement("DrugReminder", "hasActivationState")
            val sop1 = onto.inferFromOntoToReturnOPStatement(si1)

            if(medicineTaken != "False" && sop1.objectAsOwlIndividual != "True"){ //Check carefully for the type

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

                    Thread.sleep(number.roundToLong() * 60000) // Minutes
                    reasonWithSynchedTime("Instant_CurrentTimeStamp")
                    onto.saveOnto(onto.getOntoFilePath())
                    pullAndManageOnto(opStatement.subjectAsOwlIndividual)
                }
            }
        }
    }

    private fun drugReminder(opStatement: ObjectPropertyStatement) {

        val drIncState = IncompleteStatement("DrugReminderConfirmation", "hasActivationState")
        val drOPState = onto.inferFromOntoToReturnOPStatement(drIncState)
        val drIncCounter = IncompleteStatement("DrugReminderConfirmation", "hasCounter")
        val drDPCounter = onto.inferFromOntoToReturnDPStatement(drIncCounter)

        if(drOPState.objectAsOwlIndividual == "True" && drDPCounter.objectAsAnyData as Double > 0) {

            fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/confirmMedicineTaken", true) // ACTIVATES! VocalInterface
            fbDBConnector.writeDB(opStatement.subjectAsOwlIndividual+"/events/confirmMedicineTaken", false) // like a switch

            syncTimeToOnto("DrugReminderConfirmation")

            println("Just checking counter value: ${drDPCounter.objectAsAnyData} and type ${drDPCounter.objectAsAnyData.javaClass}")
            val currentCounterValue = drDPCounter.objectAsAnyData as Double
            val newCounterValue = currentCounterValue - 1

            val dpStatementCount = DataPropertyStatement("DrugReminderConfirmation", "hasCounter", newCounterValue.toLong())
            pushToOntoData(dpStatementCount)

            thread(start = true) {

                val incTimeElapsed = IncompleteStatement("DrugReminderConfirmation", "timeElapsedMinute")
                val timeElapsed = onto.inferFromOntoToReturnDPStatement(incTimeElapsed)
                val number = timeElapsed.objectAsAnyData as Double

                Thread.sleep(number.roundToLong() * 60000) // Minutes
                reasonWithSynchedTime("Instant_CurrentTimeStamp")
                // Save the Ontology
                onto.saveOnto(onto.getOntoFilePath())
                pullAndManageOnto(opStatement.subjectAsOwlIndividual)
            }
        }

        println("drugReminder Function EXECUTED")
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
}