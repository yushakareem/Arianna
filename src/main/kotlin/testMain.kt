import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.api.AnnotationsProto.http
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.FirestoreOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.SocketException
import com.sun.tools.internal.ws.wsdl.parser.Util.nextElement
import org.apache.jena.atlas.logging.Log
import sun.jvm.hotspot.debugger.win32.coff.DebugVC50X86RegisterEnums.TAG
import java.io.FileInputStream
import java.net.InetAddress
import java.util.Enumeration
import java.net.NetworkInterface
import sun.jvm.hotspot.debugger.win32.coff.DebugVC50X86RegisterEnums.TAG
import org.apache.jena.vocabulary.RDFTest.document
import com.google.cloud.firestore.DocumentReference
import arq.update
import com.google.api.core.ApiFuture
import org.apache.jena.vocabulary.RDFTest.document














object testMain {
    @JvmStatic
    fun main(args: Array<String>) {
        /*val weather = Weather()
        val actualW = weather.getWeather()

        println(actualW.weather[0].id.toInt())*/

        val url = URL("http://checkip.amazonaws.com/")
        val br = BufferedReader(InputStreamReader(url.openStream()))
        println(br.readLine())

        println("------------------")

        val serviceAccount = FileInputStream("/Users/tommasaso/Documents/Tesi/IntalliJ/vocalinterface-firebase-adminsdk-3ycvz-8068c39321.json")

        // Initialize the app with a service account, granting admin privileges
        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://vocalinterface.firebaseio.com")
                .setStorageBucket("vocalinterface.appspot.com")
                .build()
        // Initialize app for RealtimeDB
        FirebaseApp.initializeApp(options)

        val optionsFirestore = FirestoreOptions
                .newBuilder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()

        val database = FirebaseDatabase.getInstance()

        val firestoreDB = optionsFirestore.service

        val docRef = firestoreDB.collection("Home").document("Sota")
        val future1 = docRef.update("hasLocation", 6)
        val result1 = future1.get()
        val future2 = docRef.update("hasIP", "127.251.1.13")
        val result2 = future2.get()


    }
}


