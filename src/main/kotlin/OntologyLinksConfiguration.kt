/**
 * This is a dataclass. It allows it's objects to have a particular set of attributes (seen below).
 * Attributes in this dataclass hold information about all the links of a single ontology.
 */

data class OntologyLinksConfiguration(val ontoAtCenterOfLinks: Ontology) {

    var schedulerInitialDelay: Long = 0
    var schedulerIntervalPeriod: Long = 0
    var isScheduledAndObservable: Boolean = false
    lateinit var observableOntology: Ontology
    lateinit var activationStatementToObserve: ObjectPropertyStatement
    var isAnObserver: Boolean = false
    lateinit var inputDBInfo: MySqlConnector
    var mapOfDBTablesToStatements: HashMap<String, IncompleteStatement> = hashMapOf()
    lateinit var outputDBInfo: MySqlConnector
    var mapOfStatementsToDBTables: HashMap<IncompleteStatement, String> = hashMapOf()
}
