import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.FileInputStream
import io.reactivex.Observable

@Volatile private var sensorData: SensorData = SensorData("null","null")
@Volatile private var dataReadComplete: Boolean = false
@Volatile private lateinit var fbDBRef: DatabaseReference

class FirebaseConnector(private val databaseName: String, private val pathToSensors: String, private val pathToPrivateKey: String): GenericDBInterface {

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
        val fbDB = FirebaseDatabase.getInstance(app)

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

    override fun startReadData(sensorName: String, ontology : Ontology): Observable<SensorData> {

        fbDBRef.child(sensorName).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val sensorTime = dataSnapshot.child("time").value
                val sensorValue = dataSnapshot.child("value").value
                sensorData = SensorData(sensorTime, sensorValue)

                ontology.addOrUpdateToOnto(DataPropertyStatement(sensorName,"hasTimestamp",dataSnapshot.child("time").value))
                ontology.addOrUpdateToOnto(DataPropertyStatement(sensorName,"hasValue", dataSnapshot.child("value").value))
                ontology.saveOnto(ontology.getOntoFilePath())

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