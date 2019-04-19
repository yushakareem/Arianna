object MainAriannaDue {

    @JvmStatic
    fun main(args: Array<String>) {

        /** Initialize Firebase DB and Read data from sensors*/

        val fbDB2 = FirebaseConnector("vocalinterface", "/sensors", "/home/yusha/Firebase_PrivateKey/vocalinterface-firebase-adminsdk-3ycvz-ee97916161.json")
        fbDB2.connectToDB()
        fbDB2.startReadData("Light_TV")

        /** Initializing the network of Ontologies */

        val ontologiesNetwork = OntologiesNetworkBuilder()
                .withOWLOOPAnalyticsDisabled()
                //.withAriannaAnalyticsDisabled()
                .build()

        /** Initializing ontologies */

        //  Localization Ontology
        val localizationOnto = Ontology(
                "LocalizationOnto",
                "src/main/resources/WorkingOntos/LocalizationOnto.owl",
                "http://www.semanticweb.org/Arianna/LocalizationOnto",
                true
        )

        /** Initializing statements */

        //  LocalizationOnto statements
        val smartWatchLocation1 = IncompleteStatement("S_SW_Location","hasLocation")

        /** MAIN Arianna 2.0 */

        while (true) {
            if (fbDB2.getReadComplete()){
                println("There was a change!")

                // getting values from fbDB and cheking types
                println(fbDB2.getTimestamp().javaClass.toString() == "class java.lang.String")
                println(fbDB2.getValue().javaClass.toString() == "class java.lang.Long")

                // setting values to fbDB
                fbDB2.setData("PIR_TV", SensorData(fbDB2.getTimestamp(),fbDB2.getValue()))

                // update ontology with fbData
                localizationOnto.addOrUpdateToOnto(DataPropertyStatement("Light_TV","hasTimestamp",fbDB2.getTimestamp().toString()))
                localizationOnto.addOrUpdateToOnto(DataPropertyStatement("Light_TV","hasValue",fbDB2.getValue().toString()))
                localizationOnto.saveOnto(localizationOnto.getOntoFilePath())

                fbDB2.resetReadComplete()
            }
        }
    }
}
