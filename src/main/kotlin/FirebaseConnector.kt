
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import java.io.FileInputStream

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


@Volatile private var dataReadComplete: Boolean = false

class FirebaseConnector(private val databaseName: String, private val pathToPrivateKey: String, private val pathToNode: String) {

    private var realtimeDBRef: DatabaseReference
    private var firestoreDB: Firestore
    private lateinit var ontoTaskManager: OntoTaskManager

    init {
        // Fetch the service account key JSON file contents
        val serviceAccount = FileInputStream(pathToPrivateKey)

        // Initialize the app with a service account, granting admin privileges
        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://$databaseName.firebaseio.com")
                .build()
        // Initialize app for RealtimeDB
        val app = FirebaseApp.initializeApp(options)
        // Retrieve RealtimeDB service by passing the FirebaseApp object
        val fbRealtimeDB = FirebaseDatabase.getInstance(app)

        val optionsFirestore = FirestoreOptions
                .newBuilder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()

        firestoreDB = optionsFirestore.service
        realtimeDBRef = fbRealtimeDB.reference
    }

    fun checkUserNodes(onto: OntoTaskManager) {
        ontoTaskManager = onto

        val sotaLocation = DataPropertyStatement("Sota", "isRobotInLocation", checkSota())
        ontoTaskManager.pushToOntoData(sotaLocation)

        realtimeDBRef.child(pathToNode).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    //println(it)
                    checkUserLocation(it.key.toString(),"location")
                    Thread.sleep(1000) //To do in a different way
                    if(it.toString().contains("events={")){
                        checkUserDrugReminderStatus(it.key.toString(), "events/drugReminderStatus")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }

    fun checkUserLocation(userNode: String, dataNode: String) {
        // We check the location of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(dataNode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val location = ontoTaskManager.locationMapper(dataSnapshot.value.toString())
                println("${userNode} is in: ${location}")

                val dpStatement1 = DataPropertyStatement(userNode, "isHumanInLocation", location)

                ontoTaskManager.pushToOntoData(dpStatement1)
                ontoTaskManager.reasonWithSynchedTime("Instant_CurrentTime")

                ontoTaskManager.pullAndManageOnto(userNode)

                dataReadComplete = true
            }

            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }
    fun checkUserDrugReminderStatus(userNode: String, path: String) {
        // We check the location of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println("Entered into OnChange DR Status")
                val data = ontoTaskManager.statusMapper(dataSnapshot.value.toString())
                val dpStatement1 = DataPropertyStatement(userNode, "hasCurrentStatusDrugReminder", data)
                ontoTaskManager.pushToOntoData(dpStatement1)

                if(data == "succeed"){
                    TODO("remove all the stuff realted to drug reminder with the function above")
                    //ontoTaskManager.onto.breakStatementInOnto(dpStatement1)
                }
                dataReadComplete = true
                dataReadComplete = true
            }
            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }

    fun resetReadComplete() {
        dataReadComplete = false
    }

    fun getReadComplete(): Boolean {
        return dataReadComplete
    }

    fun checkDrugUser(user: String, field: String): Any {
        // asynchronously retrieve all users
        val docRef = firestoreDB.collection("usersModel").document(user).collection("drugs").document("morningOnFullStomach")
        val future = docRef.get()
        val document = future.get()
        if (document.exists()) {
            return document.get(field)!!
        } else {
            error("Error reading on Firestore: drug list doesn't present")
        }
    }
    fun checkTimeElapse(user: String, priority: String): Long {
        // asynchronously retrieve all users
        val docRef = firestoreDB.collection("usersModel").document(user).collection("timeElapse").document("forPriority")
        val future = docRef.get()
        val document = future.get()
        if (document.exists()) {
            return document.get(priority)!! as Long
        } else {
            error("Error reading on Firestore: drug list doesn't present")
        }
    }

    fun checkSota(): Any {
        // asynchronously retrieve all users
        val docRef = firestoreDB.collection("Home").document("Sota")
        val future = docRef.get()
        val document = future.get()
        if (document.exists()) {
            return ontoTaskManager.locationMapper(document.get("hasLocation").toString())
        } else {
            error("Error reading on Firestore: Sota position is not present")
        }
    }

    fun writeDB(path: String, value: Any) {
        realtimeDBRef.child("$pathToNode/$path").setValueAsync(value)
    }

}



