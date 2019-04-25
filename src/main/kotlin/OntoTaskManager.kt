import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import java.util.*

class OntoTaskManager(private val onto: Ontology) {


    fun pushToOnto(listOfStatements: List<DataPropertyStatement>) {

        // Statements which go to Ontology
        listOfStatements.forEach {
            onto.addOrUpdateToOnto(it)
        }

        // Update Ontology with CurrentTimestamp and Synchronize reasoner
        reasonWithSynchedTime("Instant_CurrentTimeStamp")

        // Save the Ontology
        onto.saveOnto(onto.getOntoFilePath())
    }

    fun pullAndManageOnto() {

        // Read latest inferences from the ontology
        val si1 = IncompleteStatement("User", "isDoingActivity")
        val sop1 = onto.inferFromOntoToReturnOPStatement(si1)
        val si2 = IncompleteStatement("DrugReminder", "hasActivationState")
        val sop2 = onto.inferFromOntoToReturnOPStatement(si2)

        // An Observable of the inference
        val userObservable = Observable.just(sop1)

        userObservable
                .subscribeBy {
                    println(it.getObject())
                }

        val taskObservable = Observable.just(sop2)
        taskObservable
                .filter { sop2.getObject() == "True" }
                .subscribeBy {
                    println(it.getObject())
                }
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