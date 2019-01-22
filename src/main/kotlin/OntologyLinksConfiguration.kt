data class OntologyLinksConfiguration(val ontoAtCenterOfLinks: Ontology) {

    var schedulerInitialDelay: Long = 0
    var schedulerIntervalPeriod: Long = 0
    var isActivatedByScheduler: Boolean = false
    lateinit var activatorOntology: Ontology
    lateinit var activationStatement: ObjectPropertyStatement
    var isActivatedByOntology: Boolean = false
    lateinit var inputDBInfo: MySqlConnector
    var mapDBTableToStatement: HashMap<String, IncompleteStatement> = hashMapOf()
    lateinit var outputDBInfo: MySqlConnector
    var mapStatementToDBTable: HashMap<IncompleteStatement, String> = hashMapOf()
}
