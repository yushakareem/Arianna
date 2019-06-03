/**
 * StorageConnector class.
 *
 * This class is used for the configuration and initialization of a storage reference.
 *
 */

// UTILS
import kotlin.collections.*
import java.io.*
import java.nio.file.Paths
import java.io.InputStream
import java.io.File
import java.nio.charset.Charset
import java.io.StringReader
import java.util.regex.Pattern

// Imports the Google Cloud client library
import com.google.firebase.cloud.StorageClient

// JSON
import com.beust.klaxon.*

import com.google.firebase.database.FirebaseDatabase


class CounterStorageConnector(private val storageClient : StorageClient) {
    //private var fireApp: FirebaseApp
    //private var storageClient : StorageClient
    private var configAlertsMap: MutableMap<String,Int> = mutableMapOf()
    val filenameJson = "alerts.json"

    // 0. Instantiate a Google Cloud Storage client
    init {
        /*storageClient = StorageClient.getInstance(firebaseApp)
        println("Connected to the Storage.")*/
    }

    private fun downloadFile(filename: String) {
        val configFileRef = storageClient.bucket().get("installation_test_name/$filename")
        println(configFileRef.exists())
        if (configFileRef.exists()){
            //println("Downloading configuration file...")
            val destFilePath = Paths.get(filename)
            //println(destFilePath)
            configFileRef.downloadTo(destFilePath)
            //println("Download completed.")
        } else {
            println("The file does not exist.")
        }
    }

    private fun loadFile(filename: String): String {
        //Create input stream
        val file = File(filename)
        var ins:InputStream = file.inputStream()
        // read contents of IntputStream to String
        var content = ins.readBytes().toString(Charset.defaultCharset())
        //println(content)
        return content
    }


    private fun parseFile(content: String){

        val pathMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = Pattern.matches(".*floors.*", path)

            override fun onMatch(path: String, value: Any) {

                if (path.contains("room") && !path.contains("Numer") ){
                    //println("Adding $path = $value")
                    val roomName = path.substring(path.length - 5)
                    configAlertsMap[roomName] = value.toString().toInt()
                }
            }
        }

        Klaxon()
            .pathMatcher(pathMatcher)
            .parseJsonObject(StringReader(content))

    }


    fun config(): MutableMap<String, Int> {

        // 1. Get the file from the storage if doesn't exist in the local directory
        /*if (!File(filenameJson).exists()) {
            downloadFile(filenameJson)
        }*/
        downloadFile(filenameJson)

        // 2. Load and Read the file
        val content = loadFile(filenameJson)
        // 3. Parse file
        parseFile(content)

        //println(configAlertsMap.entries)
        //println("Alerts configuration done.")

        return configAlertsMap

    }
}




