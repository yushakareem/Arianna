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

object FirebaseTexting {

    @Volatile var dataReadComplete: Boolean = false

    @JvmStatic
    fun main(args: Array<String>) {

        lateinit var s:SensorData

        // INITIAL PARAMETERS
        lateinit var serviceAccount: FileInputStream
        try {
            // Fetch the service account key JSON file contents
            serviceAccount = FileInputStream("/home/yusha/Firebase_PrivateKey/vocalinterface-firebase-adminsdk-3ycvz-ee97916161.json")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

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

        val app = FirebaseApp.initializeApp(options) // CONNECT TO DB

        // Retrieve services by passing the FirebaseApp object
        val fbAuth = FirebaseAuth.getInstance(app)
        val fbDB = FirebaseDatabase.getInstance(app)

        // Get reference to a node
        val ref = fbDB.getReference("/sensors") // PATH TO THE SENSOR wherein lies (time) & (value)

        ref.child("Light_TV").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                s = SensorData(dataSnapshot.child("time").value.toString(),dataSnapshot.child("value").value.toString())
                dataReadComplete = true
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // The right thing should be volatile
        while (true) {
            if (dataReadComplete){
                println("There was a change!")
                println(s.time)
                println(s.value)
                dataReadComplete = false
            }
        }
    }
}
