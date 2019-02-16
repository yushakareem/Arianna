/**
 * A builder class for building an object of class OntologyLinksConfiguration().
 * Allows to build the links for an ontology in a network.
 *
 * @return InputDBBuilder, DBTableToOntoLinkBuilder, OutputDBBuilder, OntoToDBTableLinkBuilder, OntologyLinksConfiguration
 */

class OntologyLinksBuilder(ontoAtCenterOfLinks: Ontology) {

    private val ontoLinksConfig = OntologyLinksConfiguration(ontoAtCenterOfLinks)

    fun isScheduledAndObservable(initialDelay: Long, periodicIntervalMillis: Long): InputDBBuilder {

        ontoLinksConfig.schedulerInitialDelay = initialDelay
        ontoLinksConfig.schedulerIntervalPeriod = periodicIntervalMillis
        ontoLinksConfig.isScheduledAndObservable = true
        return InputDBBuilder(ontoLinksConfig)
    }

    fun isAnObserverOnto(activatedByObservableOntology: Ontology, activationStatementToObserve: ObjectPropertyStatement): InputDBBuilder {

        ontoLinksConfig.observableOntology = activatedByObservableOntology
        ontoLinksConfig.activationStatementToObserve = activationStatementToObserve
        ontoLinksConfig.isAnObserver = true
        return InputDBBuilder(ontoLinksConfig)
    }

    class InputDBBuilder(private val ontoLinksConfig: OntologyLinksConfiguration) {

        fun inputIsFromDB(dataBaseInfo: MySqlConnector): DBTableToOntoLinkBuilder {

            ontoLinksConfig.inputDBInfo = dataBaseInfo
            return DBTableToOntoLinkBuilder(ontoLinksConfig)
        }

        class DBTableToOntoLinkBuilder(private val ontoLinksConfig: OntologyLinksConfiguration) {

            fun linkDBTableToStatementInOnto(tableNameInDataBase: String, incompleteStatement: IncompleteStatement): DBTableToOntoLinkBuilder {

                ontoLinksConfig.mapOfDBTablesToStatements[tableNameInDataBase] = incompleteStatement
                return DBTableToOntoLinkBuilder(ontoLinksConfig)
            }

            fun linksCompleted(): OutputDBBuilder {

                return OutputDBBuilder(ontoLinksConfig)
            }

        }
    }

    class OutputDBBuilder(private val ontoLinksConfig: OntologyLinksConfiguration) {

        fun outputIsToDB(dataBaseInfo: MySqlConnector): OntoToDBTableLinkBuilder {

            ontoLinksConfig.outputDBInfo = dataBaseInfo
            return OntoToDBTableLinkBuilder(ontoLinksConfig)
        }

        class OntoToDBTableLinkBuilder(private val ontoLinksConfig: OntologyLinksConfiguration){

            fun linkStatementInOntoToDBTable(incompleteStatement: IncompleteStatement, tableNameInDataBase: String): OntoToDBTableLinkBuilder {

                ontoLinksConfig.mapOfStatementsToDBTables[incompleteStatement] = tableNameInDataBase
                return OntoToDBTableLinkBuilder(ontoLinksConfig)
            }

            fun build(): OntologyLinksConfiguration {

                return ontoLinksConfig
            }
        }
    }
}