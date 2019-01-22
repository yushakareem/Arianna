class OntologyLinksBuilder(ontoAtCenterOfLinks: Ontology) {

    private val ontoLinksConfig = OntologyLinksConfiguration(ontoAtCenterOfLinks)

    fun activatedByScheduler(initialDelay: Long, periodicIntervalMillis: Long): InputDBBuilder {

        ontoLinksConfig.schedulerInitialDelay = initialDelay
        ontoLinksConfig.schedulerIntervalPeriod = periodicIntervalMillis
        ontoLinksConfig.isActivatedByScheduler = true
        return InputDBBuilder(ontoLinksConfig)
    }

    fun activatedByOntology(activatorOntology: Ontology, activationStatement: ObjectPropertyStatement): InputDBBuilder {

        ontoLinksConfig.activatorOntology = activatorOntology
        ontoLinksConfig.activationStatement = activationStatement
        ontoLinksConfig.isActivatedByOntology = true
        return InputDBBuilder(ontoLinksConfig)
    }

    class InputDBBuilder(private val ontoLinksConfig: OntologyLinksConfiguration) {

        fun inputIsFromDB(dataBaseInfo: MySqlConnector): DBTableToOntoLinkBuilder {

            ontoLinksConfig.inputDBInfo = dataBaseInfo
            return DBTableToOntoLinkBuilder(ontoLinksConfig)
        }

        class DBTableToOntoLinkBuilder(private val ontoLinksConfig: OntologyLinksConfiguration) {

            fun linkDBTableToStatementInOnto(tableNameInDataBase: String, incompleteStatement: IncompleteStatement): DBTableToOntoLinkBuilder {

                ontoLinksConfig.mapDBTableToStatement[tableNameInDataBase] = incompleteStatement
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

                ontoLinksConfig.mapStatementToDBTable[incompleteStatement] = tableNameInDataBase
                return OntoToDBTableLinkBuilder(ontoLinksConfig)
            }

            fun build(): OntologyLinksConfiguration {

                return ontoLinksConfig
            }
        }
    }
}