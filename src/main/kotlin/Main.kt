import java.util.concurrent.TimeUnit

/**
 *  This is the story board.
 */

fun main(args: Array<String>) {

    // -Initializing database
    val db = MySqlConnector("AriannaDB", "root", "nepo")

    // -Initializing ontologies
    val placeOnto = Ontology(
            "po",
            "src/main/resources/WorkingOntos/PlaceOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/PlaceOntology",
            true
    )

    val kitchenActOnto = Ontology(
            "kao",
            "src/main/resources/WorkingOntos/KitchenActivityOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/KitchenActivityOntology",
            true
    )

    val livingRoomActOnto = Ontology(
            "lrao",
            "src/main/resources/WorkingOntos/LivingRoomActivityOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/LivingRoomActivityOntology",
            true
    )

    // -Initializing statements
    // --Common statements
    val outputHARIncStmt = IncompleteStatement("H_Yusha", "isDoingActvity")

    // --PlaceOnto statements
    val swLocationIncStmt = IncompleteStatement("S_SW_Location","hasLocation")

    // --KitchenActOnto statements
    val kitchenActActivationStmt = ObjectPropertyStatement("H_Yusha", "isDoingActivity", "BeingIn_Kitchen")
    val mKitchenCabinetIncStmt = IncompleteStatement("S_M_KitchenCabinet", "detectsMotion")
    val mKitchenSinkOrStoveIncStmt = IncompleteStatement("S_M_KitchenSinkOrStove", "detectsMotion")


    // -Initializing ontology links
    val placeOntoLinks = OntologyLinksBuilder(placeOnto)
            .activatedBySchedular(0,2000,TimeUnit.MILLISECONDS)
            .inputIsFromDB(db)
            .linkTableWithIncompleteStatement("Estimote_Location_SmartWatch1", swLocationIncStmt)
            .linkingComplete()
            .outputIsToDB(db)
            .linkIncompleteStatementWithTable(outputHARIncStmt, "HAR_Output_PlaceOnto")
            .build()

    val kitchenActOntoLinks = OntologyLinksBuilder(kitchenActOnto)
            .activatedByOntology(placeOnto, kitchenActActivationStmt)
            .inputIsFromDB(db)
            .linkTableWithIncompleteStatement("PIR_KitchenCabinet", mKitchenCabinetIncStmt)
            .linkTableWithIncompleteStatement("PIR_KitchenSinkOrStove", mKitchenSinkOrStoveIncStmt)
            .linkingComplete()
            .outputIsToDB(db)
            .linkIncompleteStatementWithTable(outputHARIncStmt, "HAR_Output_KitchenActOnto")
            .build()

    // -Initializing network of Ontologies
    val ontologiesNetwork = OntologiesNetworkBuilder()
            //.withAnalyticsDisabled()
            .build()

    // -Starting the network
    val ontologiesNetworkHandler = ontologiesNetwork.startNetworking(placeOntoLinks, kitchenActOntoLinks)
}