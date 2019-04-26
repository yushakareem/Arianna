import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import java.io.FileInputStream

@Volatile private var dataReadComplete: Boolean = false

class FirebaseConnector(private val databaseName: String, private val pathToPrivateKey: String): GenericDBInterface {

    private var realtimeDBRef: DatabaseReference
    private var firestoreDB: Firestore

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


        println(firestoreDB.collection("usersModel"))

        // asynchronously retrieve all users
        val docRef = firestoreDB.collection("usersModel").document("5fe6b3ba-2767-4669-ae69-6fdc402e695e")

        val future = docRef.get()
        val document = future.get()
        if (document.exists()){
            println("works!  ${document.get("name")}")
        }else{
            println("Doc Does not Exist!")
        }

        realtimeDBRef = fbRealtimeDB.reference
    }

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
        val fbDB = FirebaseDatabase.getInstance(app)
        realtimeDBRef = fbDB.reference

        return realtimeDBRef
    }

    override fun readNodeData(pathToNode: String, userNode: String, dataNode: String, ontoTaskManager: OntoTaskManager) {

        // We check the location of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(dataNode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                println("Entered into OnChange")

                val location = ontoTaskManager.locationMapper(dataSnapshot.value.toString())

                println("Location from FB: $location")

                val dpStatement1 = DataPropertyStatement(userNode, "hasLocation", location)
                val statementList : List<DataPropertyStatement> = listOf(dpStatement1)

                ontoTaskManager.pushToOnto(statementList)
                ontoTaskManager.pullAndManageOnto(userNode)

                dataReadComplete = true
            }

            override fun onCancelled(error: DatabaseError) {

                error(error)
            }
        })
    }

    fun checkUserNode(pathToNode: String, dataNode: String, ontoTaskManager: OntoTaskManager) {

        readNodeData(pathToNode,"5fe6b3ba-2767-4669-ae69-6fdc402e695e", dataNode, ontoTaskManager)
        // find the user
//        realtimeDBRef.addListenerForSingleValueEvent(object: ValueEventListener {
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

    fun resetReadComplete() {

        dataReadComplete = false
    }

    fun getReadComplete(): Boolean {

        return dataReadComplete
    }

//    fun readFirestore(pathToValue: Any): Any {
//
//    }
}

