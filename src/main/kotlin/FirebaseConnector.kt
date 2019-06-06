
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.StorageClient
import com.google.firebase.database.*
import java.io.FileInputStream

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


@Volatile private var dataReadComplete: Boolean = false

class FirebaseConnector(private val databaseName: String, private val pathToPrivateKey: String, val pathToNode: String) {

    var realtimeDBRef: DatabaseReference
    var firestoreDB: Firestore
    private lateinit var ontoTaskManager: OntoTaskManager
    var storageClient : StorageClient
    init {
        // Fetch the service account key JSON file contents
        val serviceAccount = FileInputStream(pathToPrivateKey)

        // Initialize the app with a service account, granting admin privileges
        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://$databaseName.firebaseio.com")
                .setStorageBucket("$databaseName.appspot.com")
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

        storageClient = StorageClient.getInstance(app)
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
                    checkUserGesture(it.key.toString(),"gestureId")
                    checkUserSedentary(it.key.toString(),"stoppedMinutes")
                    if(it.toString().contains("events={")){
                        checkUserDrugReminderStatus(it.key.toString(), "events/drugReminderStatus")
                        checkProposingActivityStatus(it.key.toString(), "events/proposingActivityStatus")
                        //checkProposingActivityCounter(it.key.toString(), "events/proposingNewActivity/changeIdea")
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
                ontoTaskManager.pullAndManageOnto(userNode)

                if (location == "Lost"){
                    ontoTaskManager.handleLostLocation(dpStatement1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }
    fun checkUserGesture(userNode: String, dataNode: String) {
        // We check the gesture of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(dataNode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val gestureDP = ontoTaskManager.gestureMapper(dataSnapshot.value.toString().toInt())
                println("${userNode} ${gestureDP} at ${ontoTaskManager.getTime()}")

                val dpStatement1 = DataPropertyStatement(userNode, gestureDP, ontoTaskManager.getTime())
                ontoTaskManager.pushToOntoData(dpStatement1)
                ontoTaskManager.pullAndManageOnto(userNode)
            }

            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }

    fun checkUserSedentary(userNode: String, dataNode: String) {
        // We check the sedentary duration of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(dataNode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sedentaryDuration = dataSnapshot.value.toString().toFloat()
                println("${userNode} has not moved from ${sedentaryDuration}")

                val dpStatement1 = DataPropertyStatement(userNode, "isStopped", sedentaryDuration)
                ontoTaskManager.pushToOntoData(dpStatement1)
                if(sedentaryDuration > 0.toFloat()){
                    ontoTaskManager.pullAndManageOnto(userNode)
                }
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
            }
            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }

    fun checkProposingActivityStatus(userNode: String, path: String) {
        // We check the location of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println("Entered into OnChange PA Status")
                val data = ontoTaskManager.statusMapper(dataSnapshot.value.toString())
                val dpStatement1 = DataPropertyStatement(userNode, "hasCurrentStatusProposingActivities", data)
                ontoTaskManager.pushToOntoData(dpStatement1)

                if(data == "succeed"){
                    TODO("remove all the stuff realted to drug reminder with the function above")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
    }
    fun checkProposingActivityCounter(userNode: String, path: String) {
        // We check the location of each user
        realtimeDBRef.child(pathToNode).child(userNode).child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println("Entered into OnChange PA Counter")
                val data = dataSnapshot.value.toString().toInt()
                val dpStatement1 = DataPropertyStatement(userNode, "hasProposingActivitiesCounter", data)
                ontoTaskManager.pushToOntoData(dpStatement1)

            }
            override fun onCancelled(error: DatabaseError) {
                error(error)
            }
        })
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
    fun checkInsistency(user: String, insistency: String): Long {
        // asynchronously retrieve all users
        val docRef = firestoreDB.collection("usersModel").document(user).collection("insistency").document("insistencyLevel")
        val future = docRef.get()
        val document = future.get()
        if (document.exists()) {
            return document.get(insistency)!! as Long
        } else {
            error("Error reading on Firestore: drug list doesn't present")
        }
    }


    fun fillProposingActivities(user: String) {
        /** Take all the activities proposable from Firestore - Pick and store them in Firebase */

        // Acquire information from FireStore
        val docRef = firestoreDB.collection("usersModel").document(user).collection("proposingActivities").document("externalActivities")
        val future = docRef.get()
        val document = future.get()
        if (document.exists()) {

            // Figure out how many fields on FireBase need to be filled
            realtimeDBRef.child(pathToNode).child(user).child("events").child("proposingNewActivity").child("outsideActivities").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        // size = number of possible activity that can be proposed
                        val size :Int = document.data!!.keys.size - 1

                        // I create an array containing each activity, I shuffle it and then I will take only the first n elements
                        // where n is the number of fields that i need to fill in firebase
                        val pick = (0..size).shuffled().subList(0,dataSnapshot.childrenCount.toInt())

                        // I define the first element of the array to distinguish it from the others
                        val first = pick.first()

                        // For each activity selected I push the content in a specific field in FireBase
                        pick.forEach {
                            //For the first element I need to fill the 'proposingActivity' field
                            if(it == first){
                                writeDB("$user/events/proposingActivity", document[it.toString()]!!)
                            }
                            //For all the other elements I will fill the fields under 'internalActivities'
                            else{
                                writeDB("$user/events/proposingNewActivity/outsideActivities/${pick.indexOf(it).minus(1)}", document[it.toString()]!!)
                            }
                        }
                        writeDB("$user/events/proposingNewActivity/outsideActivities/outsideActivities", true)
                    }else{
                        println("Error: there is no ProposingInternalActivities field to fill")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    error(error)
                }
            })

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



