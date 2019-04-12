import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener



object Mainmain {
    @JvmStatic
    fun main(args: Array<String>) {


        lateinit var serviceAccount: FileInputStream
        try {
            // Fetch the service account key JSON file contents
            serviceAccount = FileInputStream("/home/yusha/Firebase_PrivateKey/vocalinterface-firebase-adminsdk-3ycvz-ee97916161.json")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        // Assuming that an environment variable is set in the bashrc, showing path to firebase_privatekey
        // Initialize the app with a service account, granting admin privileges
        lateinit var options: FirebaseOptions
        try {
            options = FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://vocalinterface.firebaseio.com")
                    .build()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val app = FirebaseApp.initializeApp(options)

        // Retrieve services by passing the FirebaseApp object
        val fbAuth = FirebaseAuth.getInstance(app)
        val fbDB = FirebaseDatabase.getInstance(app)

        // Get reference to a node
        val ref = fbDB.getReference("events/proposingActivity")

        println(fbAuth)
        println(fbDB)
        println(ref)

        // As an admin, the app has access to read and write all data, regardless of Security Rules
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val document = dataSnapshot.value
                println(document)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        Thread.sleep(6000)

        ref.setValueAsync("yusha was here (from java code)")

        Thread.sleep(6000)


//        viDatabaseReference.addValueEventListener(object : ValueEventListener{
//            override fun onCancelled(error: DatabaseError?) {
//                println("ErrorInReadingFirebase: $error")
//            }
//
//            override fun onDataChange(dataSnapshot: DataSnapshot?) {
//                println("1")
//
//                if (dataSnapshot != null) {
//                    println(dataSnapshot.child("age").value.toString())
//                } else println("Its a NULL")
//            }
//        })

    }
}
