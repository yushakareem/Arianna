import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.FileInputStream

@Volatile private var dataReadComplete: Boolean = false

class FirebaseConnector(private val databaseName: String, private val pathToSensors: String, private val pathToPrivateKey: String): GenericDBInterface {

    lateinit var fbDB: FirebaseDatabase
    private lateinit var fbDBRef: DatabaseReference

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

    fun getReadComplete(): Boolean {

        return dataReadComplete
    }


    override fun readNodeData(userNode: String, dataNode: String, ontoTaskManager: OntoTaskManager) {

        // We check the location of each user
        fbDBRef.child(userNode).child(dataNode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                println("Entered into OnChange")

                val location = ontoTaskManager.locationMapper(dataSnapshot.value.toString())

                println("Location from FB: $location")

                val dpStatement = DataPropertyStatement("User", "hasLocation", location)
                val statementList : List<DataPropertyStatement> = listOf(dpStatement)

                ontoTaskManager.pushToOnto(statementList)
                ontoTaskManager.pullAndManageOnto()

                dataReadComplete = true
            }

            override fun onCancelled(error: DatabaseError) {

                error(error)
            }
        })
    }

    //                    val eventVocalInt = VocalInterfaceEvent()
//                    eventVocalInt.activateEvent(it.getObject(), userNode, fbDB)

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

    fun checkUserNode(dataNode: String, ontoTaskManager: OntoTaskManager) {

        readNodeData("5fe6b3ba-2767-4669-ae69-6fdc402e695e", dataNode, ontoTaskManager)
        // find the user
//        fbDBRef.addListenerForSingleValueEvent(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (oneElement in snapshot.children) {
//
//                    val userID = oneElement.key.toString()
//                    readNodeData(userID, dataNode, ontology)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//                error(error)
//            }
//        })
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

