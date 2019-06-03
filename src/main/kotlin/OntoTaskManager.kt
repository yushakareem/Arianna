import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.sun.tools.corba.se.idl.constExpr.Expression.zero
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import it.emarolab.owloop.aMORDescriptor.utility.individual.MORFullIndividual
import java.math.BigInteger
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

import kotlin.concurrent.thread

import kotlin.math.roundToLong

class OntoTaskManager(val onto: Ontology, private val fbDBConnector: FirebaseConnector) {

    @Volatile var isDoingActivity = DataPropertyStatement("null","null","null")
    @Volatile var drHasActivationState= DataPropertyStatement("null","null","null")
    @Volatile var isStopped= DataPropertyStatement("null","null","null")
    @Volatile var paHasStatus= DataPropertyStatement("null","null","null")


    private var userLocalizationObservable: BehaviorSubject<DataPropertyStatement>
    private var userStoppedObservable: BehaviorSubject<DataPropertyStatement>
    private var taskObservable: BehaviorSubject<DataPropertyStatement>
    private var paHasStatusObservable: BehaviorSubject<DataPropertyStatement>
    private var weather: Weather


    init {
        weather = Weather()

        userLocalizationObservable = BehaviorSubject.createDefault(isDoingActivity)
        taskObservable = BehaviorSubject.createDefault(drHasActivationState)
        userStoppedObservable = BehaviorSubject.createDefault(isStopped)
        paHasStatusObservable = BehaviorSubject.createDefault(paHasStatus)

        userLocalizationObservable
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
        userStoppedObservable
                .filter { isStopped.objectAsAnyData.toString() != "null" && isStopped.objectAsAnyData != 0.toFloat() && paHasStatus.objectAsAnyData.toString().contains("idle")}
                .subscribeBy(
                        onNext = { sedentary(it) },
                        onError = { println("onError of  sedentary") }
                )
        paHasStatusObservable
                .filter { paHasStatus.objectAsAnyData.toString().contains("active")}
                .subscribeBy(
                        onNext = { changeIdea(it)},
                        onError = { println("onError of  sedentary") }
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
        userLocalizationObservable.onNext(isDoingActivity)

        // Catch the drugReminder state statement and observe it
        val sib = IncompleteStatement(userNode, "isActiveDrugReminder")
        drHasActivationState = onto.inferFromOntoToReturnDPStatement(sib)
        //println(drHasActivationState)
        taskObservable.onNext(drHasActivationState)

        // Catch the user hasChangeIdea statement and observe it
        var sid = IncompleteStatement(userNode,"hasCurrentStatusProposingActivities")
        paHasStatus = onto.inferFromOntoToReturnDPStatement(sid)
        paHasStatusObservable.onNext(paHasStatus)

        // Catch the user isStopped statement and observe it
        var sic = IncompleteStatement(userNode,"isStopped")
        isStopped = onto.inferFromOntoToReturnDPStatement(sic)
        userStoppedObservable.onNext(isStopped)

    }

    private fun sedentary(dpStatement: DataPropertyStatement) {
        /** Manage when the user remains stopped for more than... */

        println(">>>>>>>>>>>>>sedentary")

        //Acquisition current Weather
        val actualWeather = weather.getWeather()

        //Acquisition Sota Location
        val sotaLocation = fbDBConnector.checkSota()

        // Acquisition of Proposing Activity status from Ontology
        val paIncStatus = IncompleteStatement(dpStatement.subject, "hasCurrentStatusProposingActivities")
        val paStatus = onto.inferFromOntoToReturnDPStatement(paIncStatus)

        // Acquisition of User Location from Ontology
        val userIncLocation = IncompleteStatement(dpStatement.subject, "isHumanInLocation")
        val userLocation = onto.inferFromOntoToReturnDPStatement(userIncLocation)

        if(paStatus.toString().contains("idle") && actualWeather.weather[0].id.toInt() >= 200 && actualWeather.weather[0].id.toInt() <= 803){
            fbDBConnector.writeDB("${dpStatement.subject}/events/proposingActivityStatus", "active")

            val dpStatement1 = DataPropertyStatement(dpStatement.subject.toString(), "hasCurrentStatusProposingActivities", "active")
            onto.addOrUpdateToOnto(dpStatement1)

            val insistency = fbDBConnector.checkInsistency(dpStatement.subject, "low")
            println(">>>>>insistencyRead")
            // Save the counter acquired also in the Ontology
            val paCounter = DataPropertyStatement(dpStatement.subject, "hasProposingActivitiesCounter", insistency)
            onto.addOrUpdateToOnto(paCounter)
            onto.saveOnto(onto.getOntoFilePath())

            //reasonWithSynchedTime("Instant_CurrentTime")
            changeIdea(dpStatement)
        }

        /*fbDBConnector.realtimeDBRef.child(fbDBConnector.pathToNode).child(dpStatement.subject).child("events/proposingActivityStatus").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dpStatement.objectAsAnyData as Float > 2.toFloat() && dataSnapshot.value == "idle" && actualWeather.weather[0].id.toInt() >= 200 && actualWeather.weather[0].id.toInt() <= 803){
                    //Fill ProposingActivities taking the all the possibility from firestore, picking 3 of them and sotreing them in firebase

                    fbDBConnector.fillProposingActivities(dpStatement.subject)
                    fbDBConnector.writeDB("${dpStatement.subject}/events/proposingActivityStatus", "active")

                    val timeElapse = fbDBConnector.checkTimeElapse(dpStatement.subject, "high")      //To be changed to LOW PRIORITY
                    thread(start = true) {
                        println(">>>>>PA: waitint ...")
                        Thread.sleep( timeElapse * 60000) // Minutes
                        println(">>>>>PA: Finished to wait")
                        val insistency = fbDBConnector.checkInsistency(dpStatement.subject, "low")
                        fbDBConnector.writeDB("${dpStatement.subject}/events/proposingNewActivity/changeIdea", insistency)
                        changeIdea(dpStatement)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })*/
    }

    fun changeIdea(dpStatement: DataPropertyStatement){
        //Acquisition Sota Location
        val sotaLocation = fbDBConnector.checkSota()

        // Acquisition of User Location status from Ontology
        val userIncLocation = IncompleteStatement(dpStatement.subject, "isHumanInLocation")
        val userLocation = onto.inferFromOntoToReturnDPStatement(userIncLocation)

        // We check the sedentary duration of each user
        fbDBConnector.realtimeDBRef.child(fbDBConnector.pathToNode).child(dpStatement.subject).child("events").child("proposingActivity").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val propoingActivityState = dataSnapshot.value.toString()
                if(sotaLocation.toString() == userLocation.objectAsAnyData.toString() && propoingActivityState == ""){
                    fbDBConnector.fillProposingActivities(dpStatement.subject)
                    val timeElapse = fbDBConnector.checkTimeElapse(dpStatement.subject, "high")      //To be changed to LOW PRIORITY
                    thread(start = true) {
                        println(">>>>>PA: waitint ...")
                        Thread.sleep(timeElapse * 60000) // Minutes
                        println(">>>>>PA: Finished to wait")
                        reasonWithSynchedTime("Instant_CurrentTime")
                        changeIdea(dpStatement)
                    }
                }
                else if(sotaLocation.toString() == userLocation.objectAsAnyData.toString()){
                    println(">>>>>ChengaIdea")
                    val timeElapse = fbDBConnector.checkTimeElapse(dpStatement.subject, "high")      //To be changed to LOW PRIORITY
                    thread(start = true) {

                        // Acquisition of Proposing Activity Change Idea state from Ontology
                        val paIncChangeIdea = IncompleteStatement(dpStatement.subject, "hasProposingActivitiesChangeIdea")
                        val paChangeIdea = onto.inferFromOntoToReturnDPStatement(paIncChangeIdea)

                        // Acquisition of Proposing Activity Counter state from Ontology
                        val paIncCounter = IncompleteStatement(dpStatement.subject, "hasProposingActivitiesCounter")
                        val pacCounter = onto.inferFromOntoToReturnDPStatement(paIncCounter)

                        if(paChangeIdea.objectAsAnyData == true && pacCounter.objectAsAnyData.toString().toLong() > 0.toLong()){

                            val temp = (pacCounter.objectAsAnyData.toString().toLong())-1.toLong()
                            //fbDBConnector.writeDB("${dpStatement.subject}/events/proposingNewActivity/changeIdea", temp.toInt())
                            val paCounter = DataPropertyStatement(dpStatement.subject, "hasProposingActivitiesCounter", temp)
                            onto.addOrUpdateToOnto(paCounter)
                            onto.saveOnto(onto.getOntoFilePath())

                            fbDBConnector.writeDB("${dpStatement.subject}/events/proposingNewActivity/changeIdea", false)
                            Thread.sleep(1000)
                            fbDBConnector.writeDB("${dpStatement.subject}/events/proposingNewActivity/changeIdea", true)

                            println(">>>>>PA: waitint ...")
                            Thread.sleep( timeElapse * 60000) // Minutes
                            println(">>>>>PA: Finished to wait")

                            reasonWithSynchedTime("Instant_CurrentTime")
                            pullAndManageOnto(dpStatement.subject)

                        } else if(pacCounter.objectAsAnyData.toString().toLong() <= 0.toLong() ){
                            fbDBConnector.writeDB("${dpStatement.subject}/events/proposingActivity", "")
                            fbDBConnector.writeDB("${dpStatement.subject}/events/proposingNewActivity/outsideActivities/outsideActivities", false)
                            fbDBConnector.writeDB("${dpStatement.subject}/events/proposingActivityStatus", "failed ${Timestamp(System.currentTimeMillis())}") // DeACTIVATES! VocalInterface
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })





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

            onto.saveOnto(onto.getOntoFilePath())

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
            fbDBConnector.writeDB("${opStatement.subject}/events/drugReminderFullStomach", false) // DeACTIVATES! VocalInterface

            // Update the Drug Reminder's status on the FirebaseDB as FAILED
            fbDBConnector.writeDB("${opStatement.subject}/events/drugReminderStatus", "failed ${Timestamp(System.currentTimeMillis())}") // DeACTIVATES! VocalInterface

            //DELETE ALL THE STUFF ABOUT DRUG REMINDER IN THE ONTOLOGY
            TODO( "DELETE ALL THE STUFF ABOUT DRUG REMINDER IN THE ONTOLOGY" )
        }
    }

    fun menageLost(dpStatement: DataPropertyStatement){
        fbDBConnector.realtimeDBRef.child(fbDBConnector.pathToNode).child(dpStatement.subject).child("events/").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val status = dataSnapshot.child("proposingActivityStatus").value
                val state = dataSnapshot.child("proposingActivity").value
                if(state != "" && status == "active"){
                    fbDBConnector.writeDB("${dpStatement.subject}/events/proposingActivityStatus", "succeed ${Timestamp(System.currentTimeMillis())}")
                    fbDBConnector.writeDB("${dpStatement.subject}/events/proposingActivity", "")
                    fbDBConnector.writeDB("${dpStatement.subject}/events/proposingNewActivity/outsideActivities/outsideActivities", false)
                }else if(status.toString().contains("failed", ignoreCase = true)){
                    val timeStamp = status.toString().replace("failed ", "")
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    val temporalAccessor = formatter.parse(timeStamp)
                    val localDateTime = LocalDateTime.from(temporalAccessor)
                    val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
                    val instant = Instant.from(zonedDateTime)
                    val currentTime = Instant.now()
                    val duration = Duration.between(instant, currentTime)
                    if (duration.toMinutes() in 0..30){
                        fbDBConnector.writeDB("${dpStatement.subject}/events/proposingActivityStatus", "succeed ${Timestamp(System.currentTimeMillis())}")
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }

    fun reasonWithSynchedTime(currentTimeIndividual: String) {
        // Push into the ontology the current time
        syncTimeToOnto(currentTimeIndividual)

        // Start the Ontology's reasoning
        onto.synchronizeReasoner()
    }

    fun syncTimeToOnto(currentTimeIndividual: String) {
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
            "0" -> sensorValue = "Lost"
        }
        return sensorValue
    }
    fun gestureMapper(gestureId: Number): String{
        /** Mapper to distinguish the user's location */
        lateinit var gestureName: String
        when (gestureId) {
            1 -> gestureName = "drankAt"
            2 -> gestureName = "pouredAt"
            3 -> gestureName = "stoodAt"
            4 -> gestureName = "satDownAt"
            5 -> gestureName = "walkedAt"
        }
        return gestureName
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

