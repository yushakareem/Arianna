import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import sun.rmi.runtime.Log

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

object Mainmain {

    fun main(args: Array<String>) {


        lateinit var serviceAccount: FileInputStream
        try {
            serviceAccount = FileInputStream("src/main/resources/Firebase_PrivateKey/vocalinterface-firebase-adminsdk-3ycvz-ee97916161.json")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

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
        val viDatabaseReference = fbDB.reference

        println(fbAuth)
        println(fbDB)
        println(viDatabaseReference)

        viDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError?) {
                println("ErrorInReadingFirebase: $error")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                println("1")

                if (dataSnapshot != null) {
                    println(dataSnapshot.child("age").value.toString())
                } else println("Its a NULL")
            }
        })

    }
}
