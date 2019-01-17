data class OntologyLinks(
        val schedulerInitialDelay: Int,
        val schedulerIntervalPeriod: Int,
        val activatorOntology: Ontology,
        val activationStatement: ObjectPropertyStatement,
        val activatedByScheduler: Boolean,
        val activatedByOntology: Boolean,
        val inputDB: MySqlConnector,
        val DBTableToStatement: HashMap<String, IncompleteStatement>,
        val outputDB: MySqlConnector,
        val statementToDBTable: HashMap<IncompleteStatement, String>) {

    companion object {
        fun defaultConfiguration() = OntologyLinks(
                schedulerInitialDelay = 0,
                schedulerIntervalPeriod = 2000,
                activatorOntology = Ontology(
                        "po",
                        "src/main/resources/WorkingOntos/PlaceOntology.owl",
                        "http://www.semanticweb.org/emaroLab/YushaKareem/PlaceOntology",
                        true
                ),
                activationStatement = ObjectPropertyStatement("H_Yusha", "isDoingActivity", "BeingIn_Kitchen"),
                activatedByScheduler = false,
                activatedByOntology = false,
                inputDB = MySqlConnector("AriannaDB", "root", "nepo"),
                DBTableToStatement = hashMapOf("stimote_Location_SmartWatch1" to IncompleteStatement("S_SW_Location","hasLocation")),
                outputDB = MySqlConnector("AriannaDB", "root", "nepo"),
                statementToDBTable = hashMapOf(IncompleteStatement("H_Yusha", "isDoingActvity") to "HAR_Output_KitchenActOnto")
        )
    }
}