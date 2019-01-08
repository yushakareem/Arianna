import java.util.concurrent.TimeUnit

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
            "http://www.semanticweb.org/emaroLab/YushaKareem/PlaceOntology",
            true
    )

    //// -Kitchen Ontology
    val kitchenActOnto = Ontology(
            "kao",
            "src/main/resources/WorkingOntos/KitchenActivityOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/KitchenActivityOntology",
            true
    )

//    val livingRoomActOnto = Ontology(
//            "lrao",
//            "src/main/resources/WorkingOntos/LivingRoomActivityOntology.owl",
//            "http://www.semanticweb.org/emaroLab/YushaKareem/LivingRoomActivityOntology",
//            true
//    )

    // -Initializing statements
    //// -Common statements
    val outputHAR = IncompleteStatement("H_Yusha", "isDoingActvity")

    //// -Placing Ontology statements
    val smartWatchLocation = IncompleteStatement("S_SW_Location","hasLocation")

    //// -Kitchen Ontology statements
    val kitchenActivityActivation = ObjectPropertyStatement("H_Yusha", "isDoingActivity", "BeingIn_Kitchen")

    val kitchenCabinet = IncompleteStatement("S_M_KitchenCabinet", "detectsMotion")
    val kitchenSinkOrStove = IncompleteStatement("S_M_KitchenSinkOrStove", "detectsMotion")


    // -Initializing ontology links
    //// -Placing Ontology links
    val placeOntologyLinks = OntologyLinksBuilder(placeOnto)
            .activatedBySchedular(0,2000,TimeUnit.MILLISECONDS)
            .inputIsFromDB(db)
            .linkDataBaseTableToStatementInOntology("Estimote_Location_SmartWatch1", smartWatchLocation)
            .linksCompleted()
            .outputIsToDB(db)
            .linkStatementInOntologyToDataBaseTable(outputHAR, "HAR_Output_PlaceOnto")
            .build()

    //// -Kitchen Ontology links
    val kitchenActOntoLinks = OntologyLinksBuilder(kitchenActOnto)
            .activatedByOntology(placeOnto, kitchenActivityActivation)
            .inputIsFromDB(db)
            .linkDataBaseTableToStatementInOntology("PIR_KitchenCabinet", kitchenCabinet)
            .linkDataBaseTableToStatementInOntology("PIR_KitchenSinkOrStove", kitchenSinkOrStove)
            .linksCompleted()
            .outputIsToDB(db)
            .linkStatementInOntologyToDataBaseTable(outputHAR, "HAR_Output_KitchenActOnto")
            .build()

    // -Initializing the network of Ontologies
    val ontologiesNetwork = OntologiesNetworkBuilder()
            //.withAnalyticsDisabled()
            .build()

    //// -Starting the network
    val ontologiesNetworkHandler = ontologiesNetwork.startNetworking(placeOntologyLinks, kitchenActOntoLinks)
}

