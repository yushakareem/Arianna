import com.google.api.client.util.DateTime
import it.emarolab.amor.owlDebugger.Logger
import java.sql.Timestamp
import java.util.*

object MainAriannaDue {

    @JvmStatic
    fun main(args: Array<String>) {

        /** Initializing ontologies */

        Logger.LoggerFlag.resetAllLoggingFlags() // For disabling a lot of logging

        //  Localization Ontology
        val localizationOnto = Ontology(
                "LocalizationOnto",
                "src/main/resources/WorkingOntos/LocalizationOnto.owl",
                "http://www.semanticweb.org/Arianna/LocalizationOnto",
                true
        )

        /** Initialize Firebase DB and Read data from sensors*/

        val fbDB2 = FirebaseConnector("vocalinterface", "/installation_test_name", "/home/yusha/Firebase_PrivateKey/vocalinterface-firebase-adminsdk-3ycvz-ee97916161.json")
        fbDB2.connectToDB()

        /** Activate System for each user*/

        fbDB2.checkUserNode("location", localizationOnto)

        /** MAIN Arianna 2.0 */

        while (true) {
            if (fbDB2.getReadComplete()){

                println("Entered into while flag")

//                println("Date.time is ${Date().time}, Date.Hour is ${Date().hours}, Date.Min is ${Date().minutes}")
//                println("There was a change!")
//
//                // getting values from fbDB and cheking types
//                println(fbDB2.getTimestamp().javaClass.toString() == "class java.lang.String")
//                println(fbDB2.getValue().javaClass.toString() == "class java.lang.Long")
//
//                // setting values to fbDB
//                fbDB2.setData("PIR_TV", SensorData(fbDB2.getTimestamp(),fbDB2.getValue()))



                fbDB2.resetReadComplete()
            }
        }
    }
}
