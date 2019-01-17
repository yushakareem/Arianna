class OntologyLinksBuilder(val thisOntology: Ontology) {

    private var ontoLinksConfiguration = OntologyLinks.defaultConfiguration()

    fun activatedByScheduler(initialDelay: Int, periodicIntervalMillis: Int): InputDBBuilder {
        ontoLinksConfiguration.copy(schedulerInitialDelay = initialDelay, schedulerIntervalPeriod = periodicIntervalMillis, activatedByScheduler = true)
        return InputDBBuilder(ontoLinksConfiguration)
    }

    fun activatedByOntology(activatorOntology: Ontology, activationStatement: ObjectPropertyStatement): InputDBBuilder {
        ontoLinksConfiguration.copy(activatorOntology = activatorOntology, activationStatement = activationStatement, activatedByOntology = true)
        return InputDBBuilder(ontoLinksConfiguration)
    }

    class InputDBBuilder(var ontoLinksConfig: OntologyLinks) {
        
        fun inputIsFromDB(dataBaseInfo: MySqlConnector): DatabaseTableToOntologyLinkBuilder {
            ontoLinksConfig.copy(inputDB = dataBaseInfo)
            return DatabaseTableToOntologyLinkBuilder(ontoLinksConfig)
        }
        
        class DatabaseTableToOntologyLinkBuilder(var ontoLinksConfig: OntologyLinks) {

            var DBTableToStatementMap = HashMap<String, IncompleteStatement>()

            fun linkDataBaseTableToStatementInOntology(tableNameInDataBase: String, incompleteStatement: IncompleteStatement): DatabaseTableToOntologyLinkBuilder {
                DBTableToStatementMap[tableNameInDataBase] = incompleteStatement
                ontoLinksConfig.copy(DBTableToStatement = DBTableToStatementMap)
                return DatabaseTableToOntologyLinkBuilder(ontoLinksConfig)
            }

            fun linksCompleted(): OutputDBBuilder {
                return OutputDBBuilder(ontoLinksConfig)
            }

        }
    }
    
    class OutputDBBuilder(var ontoLinksConfig: OntologyLinks) {
        
        fun outputIsToDB(dataBaseInfo: MySqlConnector): OntologyToDatabaseTableLinkBuilder {
            ontoLinksConfig.copy(outputDB = dataBaseInfo)
            return OntologyToDatabaseTableLinkBuilder(ontoLinksConfig)
        }
        
        class OntologyToDatabaseTableLinkBuilder(var ontoLinksConfig: OntologyLinks){

            var statementToDBTableMap = HashMap<IncompleteStatement, String>()

            fun linkStatementInOntologyToDataBaseTable(incompleteStatement: IncompleteStatement, tableNameInDataBase: String): OntologyToDatabaseTableLinkBuilder {
                statementToDBTableMap[incompleteStatement] = tableNameInDataBase
                ontoLinksConfig.copy(statementToDBTable = statementToDBTableMap)
                return OntologyToDatabaseTableLinkBuilder(ontoLinksConfig)
            }

            fun build(): OntologyLinks {
                return ontoLinksConfig
            }
        }
    }
}