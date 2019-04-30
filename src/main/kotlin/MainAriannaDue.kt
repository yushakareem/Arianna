import it.emarolab.amor.owlDebugger.Logger

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

        val fbDB2 = FirebaseConnector("vocalinterface","/home/yusha/Firebase_PrivateKey/vocalinterface-firebase-adminsdk-3ycvz-ee97916161.json", "/installation_test_name")
//        fbDB2.connectToDB()

        /** Initialize OntoTakManager */

        val ontoTaskManager = OntoTaskManager(localizationOnto,fbDB2)

        /** Begin */
        fbDB2.checkUserNode("location", ontoTaskManager)


        /** MAIN Arianna 2.0 */

        while (true) {
            if (fbDB2.getReadComplete()){

                println("Entered into while flag")


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
