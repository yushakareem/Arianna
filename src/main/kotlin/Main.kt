/**
 *  This is the story board.
 */

fun main(args: Array<String>) {


    /** Initializing DataBase */

    val db = MySqlConnector("AriannaDB", "root", "nepo")

    /** Initializing the network of Ontologies */

    val ontologiesNetwork = OntologiesNetworkBuilder()
            .withOWLOOPAnalyticsDisabled()
            //.withAriannaAnalyticsDisabled()
            .build()

    /** Initializing ontologies */

    //  Localization Ontology
    val LocalizationOnto = Ontology(
            "LocalizationOnto",
            "src/main/resources/WorkingOntos/LocalizationOnto.owl",
            "http://www.semanticweb.org/Arianna/LocalizationOnto",
            true
    )

    //  Kitchen Ontology
    val kitchenOnto = Ontology(
            "KitchenOnto",
            "src/main/resources/WorkingOntos/KitchenOnto.owl",
            "http://www.semanticweb.org/Arianna/KitchenOnto",
            true
    )

    //  LivingRoom Ontology
    val livingRoomOnto = Ontology(
            "LivingRoomOnto",
            "src/main/resources/WorkingOntos/LivingRoomOnto.owl",
            "http://www.semanticweb.org/Arianna/LivingRoomOnto",
            true
    )

    /** Initializing statements */

    //  Common statements
    val outputHAR = IncompleteStatement("Yusha", "isDoingActivity")

    //  LocalizationOnto statements
    val smartWatchLocation1 = IncompleteStatement("S_SW_Location","hasLocation")

    //  KitchenOnto statements
    val kitchenActivationStatement = ObjectPropertyStatement("Yusha", "isDoingActivity", "BeingIn_Kitchen")
    val kitchenCabinet = IncompleteStatement("S_M_KitchenCabinet", "detectsMotion")
    val kitchenSinkOrStove = IncompleteStatement("S_M_KitchenSinkOrStove", "detectsMotion")

    /** Initializing ontology links */

    //  LocalizationOnto links
    val linksOfLocalizationOnto = OntologyLinksBuilder(LocalizationOnto)
            .activatedByScheduler(0,10000)
            .inputIsFromDB(db)
            .linkDBTableToStatementInOnto("Estimote_Location_SmartWatch1", smartWatchLocation1)
            .linksCompleted()
            .outputIsToDB(db)
            .linkStatementInOntoToDBTable(outputHAR, "HAR_Output_PlaceOnto")
            .build()

    //  KitchenOnto links
    val linksOfKitchenOnto = OntologyLinksBuilder(kitchenOnto)
            .activatedByOntology(LocalizationOnto, kitchenActivationStatement)
            .inputIsFromDB(db)
            .linkDBTableToStatementInOnto("PIR_KitchenCabinet", kitchenCabinet)
            .linkDBTableToStatementInOnto("PIR_KitchenSinkOrStove", kitchenSinkOrStove)
            .linksCompleted()
            .outputIsToDB(db)
            .linkStatementInOntoToDBTable(outputHAR, "HAR_Output_KitchenActOnto")
            .build()

    /** Starting the network */

    val ontologiesNetworkHandler = ontologiesNetwork.startNetworking(linksOfLocalizationOnto, linksOfKitchenOnto)
}

