/**
 *  This is the story board.
 */

fun main(args: Array<String>) {

    // -Initializing database
    val db = MySqlConnector("AriannaDB", "root", "nepo")

    // -Initializing ontologies
    //// -Placing Ontology
    val placeOnto = Ontology(
            "po",
            "src/main/resources/WorkingOntos/PlaceOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/PrototypeOntology",
            true
    )

    //// -Kitchen Ontology
    val kitchenOnto = Ontology(
            "kao",
            "src/main/resources/WorkingOntos/KitchenActivityOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/KitchenActivityOntology",
            true
    )

    val livingRoomOnto = Ontology(
            "lrao",
            "src/main/resources/WorkingOntos/LivingRoomActivityOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/LivingRoomActivityOntology",
            true
    )

    // -Initializing statements
    //// -Common statements
    val outputHAR = IncompleteStatement("Yusha", "isDoingActvity")

    //// -Placing Ontology statements
    val smartWatchLocation = IncompleteStatement("S_SW_Location","hasLocation")

    //// -Kitchen Ontology statements
    val kitchenActivationStatement = ObjectPropertyStatement("Yusha", "isDoingActivity", "BeingIn_Kitchen")

    val kitchenCabinet = IncompleteStatement("S_M_KitchenCabinet", "detectsMotion")
    val kitchenSinkOrStove = IncompleteStatement("S_M_KitchenSinkOrStove", "detectsMotion")

    // -Initializing ontology links
    //// -Placing Ontology links
    val linksOfPlaceOnto = OntologyLinksBuilder(placeOnto)
            .activatedByScheduler(0,15000)
            .inputIsFromDB(db)
            .linkDBTableToStatementInOnto("Estimote_Location_SmartWatch1", smartWatchLocation)
            .linksCompleted()
            .outputIsToDB(db)
            .linkStatementInOntoToDBTable(outputHAR, "HAR_Output_PlaceOnto")
            .build()

    //// -Kitchen Ontology links
    val linksOfKitchenOnto = OntologyLinksBuilder(kitchenOnto)
            .activatedByOntology(placeOnto, kitchenActivationStatement)
            .inputIsFromDB(db)
            .linkDBTableToStatementInOnto("PIR_KitchenCabinet", kitchenCabinet)
            .linkDBTableToStatementInOnto("PIR_KitchenSinkOrStove", kitchenSinkOrStove)
            .linksCompleted()
            .outputIsToDB(db)
            .linkStatementInOntoToDBTable(outputHAR, "HAR_Output_KitchenActOnto")
            .build()

    // -Initializing the network of Ontologies
    val ontologiesNetwork = OntologiesNetworkBuilder()
            //.withAnalyticsDisabled()
            .build()

    //// -Starting the network
    val ontologiesNetworkHandler = ontologiesNetwork.startNetworking(linksOfPlaceOnto, linksOfKitchenOnto)
}

