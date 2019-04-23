import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.FileInputStream
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import java.util.*

@Volatile private var sensorData: SensorData = SensorData("null","null")
@Volatile private var dataReadComplete: Boolean = false
@Volatile private lateinit var fbDBRef: DatabaseReference

class FirebaseConnector(private val databaseName: String, private val pathToSensors: String, private val pathToPrivateKey: String): GenericDBInterface {

    lateinit var fbDB: FirebaseDatabase
    override fun connectToDB(): DatabaseReference {

        // Fetch the service account key JSON file contents
        val serviceAccount = FileInputStream(pathToPrivateKey)

        // Initialize the app with a service account, granting admin privileges
        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://$databaseName.firebaseio.com")
                .build()

        // Connect to firebase DB
        val app = FirebaseApp.initializeApp(options)

        // Retrieve services by passing the FirebaseApp object
        val fbAuth = FirebaseAuth.getInstance(app)
        fbDB = FirebaseDatabase.getInstance(app)

        // Get reference to a node, i.e., PATH TO THE SENSOR node wherein are (time) and (value)
        fbDBRef = fbDB.getReference(pathToSensors)

        return fbDBRef
    }

    /** Please setSensorName before doing get operation*/
    override fun getTimestamp(): Any {

        return sensorData.time
    }

    override fun getValue(): Any {

        return sensorData.value
    }

    fun getReadComplete(): Boolean {

        return dataReadComplete
    }


    override fun readNodeData(userNode: String, dataNode: String, ontology : Ontology): Observable<SensorData> {

        // We check the location of each user
        fbDBRef.child(userNode).child(dataNode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                println("Entered into OnChange")
                println(dataSnapshot)

                val sensorTime = Date()
                lateinit var sensorValue: String
                if (dataSnapshot.value.toString() == "6") {
                    sensorValue = "Kitchen"
                }else if (dataSnapshot.value.toString() == "2") {
                    sensorValue = "LivingRoom"
                }
                sensorData = SensorData(sensorTime, sensorValue)

                // Adding something to the Ontology
                ontology.addOrUpdateToOnto(DataPropertyStatement("Instant_CurrentTimeStamp","hour", Date().hours).assignSpecialOntoRef(ontology.getOntoRef(),ontology.getTemporalOntoRef(),ontology.getOntoRef()))
                ontology.addOrUpdateToOnto(DataPropertyStatement("SmartWatch","hasLocation", sensorData.value))
                ontology.saveOnto(ontology.getOntoFilePath())

                // Synchronize reasoner of the ontology
                ontology.synchronizeReasoner()

                // Read latest inferences from the ontology
                // The incompleteStatement
                val s1 = IncompleteStatement("User", "isDoingActivity")
                val s2 = ontology.inferFromOntoToReturnOPStatement(s1)

                val observableDoingActivity = Observable.just(ontology.inferFromOnto(s2))

                observableDoingActivity.subscribeBy {

                    val eventVocalInt = VocalInterfaceEvent()
                    eventVocalInt.activateEvent(it, userNode, fbDB)
                }

                dataReadComplete = true
            }

            override fun onCancelled(error: DatabaseError) {

                error(error)
            }
        })

        return Observable.just(sensorData)
    }

    fun resetReadComplete() {

        dataReadComplete = false
    }

//    override fun getBooleanValue(): Boolean {
//
//        lateinit var boolean: Any
//        fbDBRef.child(sensorName).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                boolean = dataSnapshot.child("value").value
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                error(error)
//            }
//        })
//        return boolean as Boolean
//    }
//
//    override fun getIntegerValue(): Int {
//
//        lateinit var integer: Any
//        fbDBRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                integer = dataSnapshot.child(sensorName).child("value").value
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                error(error)
//            }
//        })
//        return integer as Int
//    }
//
//    override fun getDoubleValue(): Double {
//
//        var double: Any = 0.0
//        fbDBRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                double = dataSnapshot.child(sensorName).child("value").value
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                error(error)
//            }
//        })
//        return double as Double
//    }
//
//    override fun getStringValue(): String {
//
//        lateinit var string: Any
//        fbDBRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                string = dataSnapshot.child(sensorName).child("value").value
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                error(error)
//            }
//        })
//        return string as String
//    }

    override fun setData(sensorName: String, sensorData: SensorData) {

        fbDBRef.child(sensorName).child("time").setValueAsync(sensorData.time)
        fbDBRef.child(sensorName).child("value").setValueAsync(sensorData.value)
    }

    fun checkUserNode(dataNode: String, ontology: Ontology) {

        // find the user
        fbDBRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (oneElement in snapshot.children) {

                    val userID = oneElement.key.toString()
                    readNodeData(userID, dataNode, ontology)
                }
            }

            override fun onCancelled(error: DatabaseError) {

                error(error)
            }
        })
    }

//    override fun setBooleanValue(timestamp: Timestamp, booleanValue: Boolean) {
//
//        fbDBRef.child(sensorName).child("time").setValueAsync(timestamp.toString())
//        fbDBRef.child(sensorName).child("value").setValueAsync(booleanValue)
//    }
//
//    override fun setIntegerValue(timestamp: Timestamp, integerValue: Int) {
//
//        fbDBRef.child(sensorName).child("time").setValueAsync(timestamp.toString())
//        fbDBRef.child(sensorName).child("value").setValueAsync(integerValue)
//    }
//
//    override fun setStringValue(timestamp: Timestamp, stringValue: String) {
//
//        fbDBRef.child(sensorName).child("time").setValueAsync(timestamp.toString())
//        fbDBRef.child(sensorName).child("value").setValueAsync(stringValue)
//    }


}

