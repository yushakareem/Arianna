/**
 *  This is the story board.
 */

fun main(args: Array<String>) {

    // Initializing database
    val db = MySqlConnector("AriannaDB", "root", "nepo")

    // Initializing ontologies
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

    // Initializing ontology links
    val placeOntoLinks = OntologyLinksBuilder(placeOnto)

    val test = ObjectPropertyStatement("Yusha","isin","theToilet")

    //po.saveOnto(po.getOntoFilePath())
}