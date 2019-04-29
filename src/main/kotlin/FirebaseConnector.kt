import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import java.io.FileInputStream

@Volatile private var dataReadComplete: Boolean = false

class FirebaseConnector(private val databaseName: String, private val pathToPrivateKey: String, private val pathToNode: String): GenericDBInterface {

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

        realtimeDBRef = fbRealtimeDB.reference
    }

     override fun readNodeData(userNode: String, dataNode: String, ontoTaskManager: OntoTaskManager) {

        // We check the location of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(dataNode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                println("Entered into OnChange")

                val location = ontoTaskManager.locationMapper(dataSnapshot.value.toString())

                println("Location from FB: $location")

                val dpStatement1 = DataPropertyStatement(userNode, "hasLocation", location)
                val statementList : List<DataPropertyStatement> = listOf(dpStatement1)

                ontoTaskManager.pushToOntoData(statementList)
                ontoTaskManager.pullAndManageOnto(userNode)

                dataReadComplete = true
            }

            override fun onCancelled(error: DatabaseError) {

                error(error)
            }
        })
    }

    fun checkUserNode( dataNode: String, ontoTaskManager: OntoTaskManager) {

        readNodeData("5fe6b3ba-2767-4669-ae69-6fdc402e695e", dataNode, ontoTaskManager)
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

    fun checkDrugUser(user: String, field: String): Any {

        // asynchronously retrieve all users
        val docRef = firestoreDB.collection("usersModel").document(user).collection("drugs").document("morningOnFullStomach")
        val future = docRef.get()
        val document = future.get()
        if (document.exists()){
            return document.get(field)!!
        }else{
            error("Error reading on Firestore: drug list doesn't present")
        }
    }

    fun writeDB(path: String, value: Any ) {

        realtimeDBRef.child("$pathToNode/$path").setValueAsync(value)
    }

    fun readDB(path: String): String{

        var any = "null"

        realtimeDBRef.child(pathToNode).child(path).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                any = snapshot.value as String
                println("In readDB: $any")
            }

            override fun onCancelled(error: DatabaseError?) {
                error("Error in reading FireBase ")
            }
        })

        return any
    }
}

