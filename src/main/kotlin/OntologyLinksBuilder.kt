import java.util.concurrent.TimeUnit

class OntologyLinksBuilder(val thisOntology: Ontology) {

    fun activatedBySchedular(initialDelay: Int, periodicInterval: Int, timeUnit: TimeUnit): InputDBBuilder {

    }

    fun activatedByOntology(activatorOntology: Ontology, activationStatement: ObjectPropertyStatement): InputDBBuilder {

    }

    class InputDBBuilder(){
        
        fun inputIsFromDB(dataBaseInfo: MySqlConnector): DatabaseTableToOntologyLinkBuilder {

        }
        
        class DatabaseTableToOntologyLinkBuilder() {
            fun linkDataBaseTableToStatementInOntology(tableNameInDataBase: String, incompleteStatement: IncompleteStatement): DatabaseTableToOntologyLinkBuilder {

            }

            fun linksCompleted(): OutputDBBuilder {

            }

        }
    }
    
    class OutputDBBuilder(){
        
        fun outputIsToDB(dataBaseInfo: MySqlConnector): OntologyToDatabaseTableLinkBuilder {

        }
        
        class OntologyToDatabaseTableLinkBuilder(){
            fun linkStatementInOntologyToDataBaseTable(incompleteStatement: IncompleteStatement, tableNameInDataBase: String): OntologyToDatabaseTableLinkBuilder {

            }

            fun build(): OntologyLinks {

            }


        }
        
    }
}