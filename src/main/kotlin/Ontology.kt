import it.emarolab.amor.owlInterface.OWLReferences
import it.emarolab.amor.owlInterface.OWLReferencesInterface
import it.emarolab.owloop.aMORDescriptor.utility.individual.MORFullIndividual
import org.apache.jena.atlas.logging.Log
import org.semanticweb.owlapi.model.OWLLiteral
import org.semanticweb.owlapi.model.OWLNamedIndividual
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter


/**
 * Please explore these links, for a clear understanding of:
 * (The idea of Ontology based on OWL and SWRL) https://dior.ics.muni.cz/~makub/owl/
 * (Common mistakes while building an ontology) https://protege.stanford.edu/conference/2004/slides/6.1_Horridge_CommonErrorsInOWL.pdf
 *
 * This class helps in initializing ontologies and manipulating them.
 *
 * @param ontoRefName is the reference name that you would like to give to this ontology.
 * @param ontoFilePath is the location (path) to the .owl file.
 * @param ontoIRI is the Internationalized Resource Identifier which can be found from Protege.
 * @param bufferingReasoner if true, causes the reasoner to take into consideration the changes in the current root ontology.
 */
class Ontology(ontoRefName: String, private val ontoFilePath: String, ontoIRI: String, bufferingReasoner: Boolean) {

    private val temporalOntoRef: OWLReferences
    private val ontoRef: OWLReferences

    init {

        val owlTimeOntoIRI = "http://www.w3.org/2006/time"
        val owlTimeOntoRef = "owlTimeOntoRef"

        ontoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(ontoRefName, ontoFilePath, ontoIRI, bufferingReasoner)
        temporalOntoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(owlTimeOntoRef, ontoFilePath, owlTimeOntoIRI, bufferingReasoner)
    }

    //ADD statements
    /**
     * Adds ObjectPropertyStatement to the Ontology.
     */
    fun addToOnto(statement: ObjectPropertyStatement) {

        if (statement.madeOfSpecialOntoRef) {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual , statement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                addObject(statement.ontoRefForVerb.getOWLObjectProperty(statement.verbAsOwlProperty), statement.ontoRefForObject.getOWLIndividual(statement.objectAsOwlIndividual))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual, getOntoRef())
            individual.apply {
                readSemantic()
                addObject(getOntoRef().getOWLObjectProperty(statement.verbAsOwlProperty), getOntoRef().getOWLIndividual(statement.objectAsOwlIndividual))
                writeSemantic()
            }
        }
    }
    /**
     * Adds DataPropertyStatement to the Ontology.
     */
    fun addToOnto(statement: DataPropertyStatement) {

        val individual = MORFullIndividual(statement.subjectAsOwlIndividual ,this.ontoRef)
        individual.apply {
            readSemantic()
            when {
                statement.madeWithObjectAsString     -> addData(statement.verbAsOwlProperty, statement.objectAsStringData)
                statement.madeWithObjectAsTimestamp  -> addData(statement.verbAsOwlProperty, statement.objectAsTimestampData.toString())
                statement.madeWithObjectAsBoolean    -> addData(statement.verbAsOwlProperty, statement.objectAsBooleanData,true)
                statement.madeWithObjectAsDouble    -> addData(statement.verbAsOwlProperty, statement.objectAsDoubleData)
                else -> Log.debug("==Error==> ","IncompleteStatement not correctly created or initialized.")
            }
            writeSemantic()
        }
    }

    //INFER from statements
    /**
     *  Infers from Ontology the 'object' of the ObjectPropertyStatement; returns String.
     */
    fun inferFromOnto(opStatement: ObjectPropertyStatement): String {

        lateinit var individual: MORFullIndividual
        lateinit var namedIndiv: OWLNamedIndividual

        if (opStatement.madeOfSpecialOntoRef) {
            individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, opStatement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                namedIndiv = individual.getObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty))
            }
        } else {
            individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, this.ontoRef)
            individual.apply {
                readSemantic()
                namedIndiv = individual.getObject(opStatement.verbAsOwlProperty)
            }
        }

        return individual.getOWLName(namedIndiv)
    }
    /**
     *  Infers from Ontology the 'object' of the DataPropertyStatement; returns String.
     */
    fun inferFromOnto(dpStatement: DataPropertyStatement): String {
        val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual , this.ontoRef)
        lateinit var data: OWLLiteral

        individual.apply {
            readSemantic()
            data = individual.dataSemantics.getLiteral(individual.getOWLDataProperty(dpStatement.verbAsOwlProperty))
        }

        return data.literal
    }
    /**
     *  Infers from Ontology the 'object' of the ObjectPropertyStatement; returns ObjectPropertyStatement.
     */
    fun inferFromOntoToReturnOPStatement(incompleteStatement: IncompleteStatement): ObjectPropertyStatement {
        lateinit var individual: MORFullIndividual
        lateinit var namedIndiv: OWLNamedIndividual
        val inferredObjectAsOWLIndividual: String
        val opStatement: ObjectPropertyStatement

        try {
            if (incompleteStatement.madeOfSpecialOntoRef) {
                individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, incompleteStatement.ontoRefForSubject)
                individual.apply {
                    readSemantic()
                    namedIndiv = individual.getObject(incompleteStatement.ontoRefForVerb.getOWLObjectProperty(incompleteStatement.verbAsOwlProperty))
                }
                inferredObjectAsOWLIndividual = individual.getOWLName(namedIndiv)
                opStatement = ObjectPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty,inferredObjectAsOWLIndividual).assignSpecialOntoRef(incompleteStatement.getSpecialSubjectOntoRef(),incompleteStatement.getSpecialVerbOntoRef(),getOntoRef())
            } else {
                individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, this.ontoRef)
                individual.apply {
                    readSemantic()
                    namedIndiv = individual.getObject(incompleteStatement.verbAsOwlProperty)
                }
                inferredObjectAsOWLIndividual = individual.getOWLName(namedIndiv)
                opStatement = ObjectPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty,inferredObjectAsOWLIndividual)
            }
        } catch (e: IllegalStateException) {
            error("The inference of IncompleteStatement is NULL. Please handle this carefully.")
        }

        return opStatement
    }
    /**
     *  Infers from Ontology the 'object' of the ObjectPropertyStatement; returns DataPropertyStatement.
     */
    fun inferFromOntoToReturnDPStatement(incompleteStatement: IncompleteStatement): DataPropertyStatement {
        val individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual , this.ontoRef)
        lateinit var data: OWLLiteral
        lateinit var dpStatement: DataPropertyStatement
        var inferredString: String

        var dataAsSting: String
        var dataAsBoolean: Boolean
        var dataAsDouble: Double?
        lateinit var dataAsTimestamp: Timestamp

        val booleanList = listOf<String>("true","True","false","False")

        try {
            individual.apply {
                readSemantic()
                data = individual.dataSemantics.getLiteral(individual.getOWLDataProperty(incompleteStatement.verbAsOwlProperty))
                inferredString = data.literal

                dataAsSting = inferredString
                dataAsDouble = inferredString.toDoubleOrNull()

                when {
                    inferredString.matches("(\\d+\\-\\d+\\-\\d+\\ \\d+\\:\\d+\\:\\d+\\.\\d+)?".toRegex()) -> {
                        //data is of type Timestamp
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS")
                        val parsedDate = dateFormat.parse(inferredString)
                        dataAsTimestamp = Timestamp(parsedDate.time)
                        dpStatement = DataPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(),dataAsTimestamp)
                    }
                    inferredString in booleanList -> {
                        //data is of type Boolean
                        dataAsBoolean = inferredString.toBoolean()
                        dpStatement = DataPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(),dataAsBoolean)
                    }
                    dataAsDouble != null -> {
                        //data is of type Double
                        dpStatement = DataPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(), dataAsDouble!!)
                    }
                    else -> {
                        //data is of type String
                        dpStatement = DataPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(), dataAsSting)
                    }
                }
            }
        } catch (e: IllegalStateException) {
            error("The inference of IncompleteStatement is NULL. Please handle this carefully.")
        }

        return dpStatement
    }

    //ADD or UPDATE statements
    /**
     *  Adds (if does not exist already) or Updates (if exists already), the ObjectPropertyStatement.
     */
    fun addOrUpdateToOnto(opStatement: ObjectPropertyStatement, withReplacement: Boolean) {

        if (opStatement.madeOfSpecialOntoRef) {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual , opStatement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                try {
                    val inference = inferFromOnto(opStatement)
                    if (inference.contentEquals(opStatement.getObject())) {
                        //The statement's inference is the same, nothing to update.
                    } else {
                        if (withReplacement) {
                            //The statement's inference is not the same, so update with replacement.
                            //(If the inference holds more than one result, then the previous-last one gets replaced)
                            removeObjectFromOnto(inference,opStatement.ontoRefForObject)
                            addObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsOwlIndividual))
                        } else {
                            //The statement's inference is not the same, so update without replacement.
                            addObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsOwlIndividual))
                        }
                    }
                    addObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsOwlIndividual))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    //When the result of inferFromOnto is 'null' we jump to catch
                    addObject(ontoRef.getOWLObjectProperty(opStatement.verbAsOwlProperty), ontoRef.getOWLIndividual(opStatement.objectAsOwlIndividual))
                    writeSemantic()
                }
            }
        } else {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, this.ontoRef)
            individual.apply {
                readSemantic()
                try {
                    val inference = inferFromOnto(opStatement)
                    if (inference.contentEquals(opStatement.getObject())) {
                        //The statement's inference is the same, nothing to update.
                    } else {
                        if (withReplacement) {
                            //The statement's inference is not the same, so update with replacement.
                            removeObjectFromOnto(inference,getOntoRef())
                            addObject(getOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), getOntoRef().getOWLIndividual(opStatement.objectAsOwlIndividual))
                        } else {
                            //The statement's inference is not the same, so update without replacement.
                            addObject(getOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), getOntoRef().getOWLIndividual(opStatement.objectAsOwlIndividual))
                        }
                    }
                    addObject(ontoRef.getOWLObjectProperty(opStatement.verbAsOwlProperty), ontoRef.getOWLIndividual(opStatement.objectAsOwlIndividual))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    //When the result of inferFromOnto is 'null' we jump to catch
                    addObject(ontoRef.getOWLObjectProperty(opStatement.verbAsOwlProperty), ontoRef.getOWLIndividual(opStatement.objectAsOwlIndividual))
                    writeSemantic()
                }
            }
        }
    }
    /**
     *  Adds (if does not exist already) or Updates (if exists already), the DataPropertyStatement.
     */
    fun addOrUpdateToOnto(dpStatement: DataPropertyStatement) {

        val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual ,this.ontoRef)
        individual.apply {
            readSemantic()
            removeData(dpStatement.verbAsOwlProperty)
            when {
                dpStatement.madeWithObjectAsString     -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsStringData)
                dpStatement.madeWithObjectAsTimestamp  -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsTimestampData.toString())
                dpStatement.madeWithObjectAsBoolean    -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsBooleanData,true)
                dpStatement.madeWithObjectAsDouble    -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsDoubleData)
                else -> Log.debug("==Error==> ","IncompleteStatement not correctly created or initialized.")
            }
            writeSemantic()
        }
    }

    //BREAK statement
    /**
     * Breaks the ObjectPropertyStatement.
     * Means that the subject, verb and object still exist in the ontology but are not related to eachother.
     */
    fun breakStatementInOnto(opStatement: ObjectPropertyStatement) {

        if (opStatement.madeOfSpecialOntoRef) {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual , opStatement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                removeObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsOwlIndividual))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, this.ontoRef)
            individual.apply {
                readSemantic()
                removeObject(opStatement.verbAsOwlProperty, opStatement.objectAsOwlIndividual)
                writeSemantic()
            }
        }
    }

    //REMOVE parts of the statement
    /**
     * Removes subject from Ontology.
     */
    fun removeSubjectFromOnto(name: String, ontoRef: OWLReferences) {

        ontoRef.removeIndividual(name)
    }
    /**
     * Removes subject from Ontology.
     */
    fun removeSubjectFromOnto(opStatement: ObjectPropertyStatement) {

        if (opStatement.isMadeOfSpecialOntoRef()) {
            opStatement.getSpecialSubjectOntoRef().removeIndividual(opStatement.subjectAsOwlIndividual)
        } else {
            getOntoRef().removeIndividual(opStatement.subjectAsOwlIndividual)
        }
    }
    /**
     * Removes subject from Ontology.
     */
    fun removeSubjectFromOnto(dpStatement: DataPropertyStatement) {

        if (dpStatement.isMadeOfSpecialOntoRef()) {
            dpStatement.getSpecialSubjectOntoRef().removeIndividual(dpStatement.subjectAsOwlIndividual)
        } else {
            getOntoRef().removeIndividual(dpStatement.subjectAsOwlIndividual)
        }
    }
    /**
     * Removes verb from Ontology.
     */
    fun removeVerbFromOntology(opStatement: ObjectPropertyStatement) {
        //For now not useful
    }
    /**
     * Removes verb from Ontology.
     */
    fun removeVerbFromOntology(dpStatement: DataPropertyStatement) {
        //For now not useful
    }
    /**
     * Removes object from Ontology.
     */
    fun removeObjectFromOnto(name: String, ontoRef: OWLReferences) {

        ontoRef.removeIndividual(name)
    }
    /**
     * Removes object from Ontology.
     */
    fun removeObjectFromOnto(opStatement: ObjectPropertyStatement) {

        if (opStatement.isMadeOfSpecialOntoRef()) {
            opStatement.getSpecialObjectOntoRef().removeIndividual(opStatement.objectAsOwlIndividual)
        } else {
            getOntoRef().removeIndividual(opStatement.objectAsOwlIndividual)
        }
    }
    /**
     * Removes object from Ontology.
     */
    fun removeObjectFromOnto(dpStatement: DataPropertyStatement) {

        val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual , this.ontoRef)
        individual.apply {
            readSemantic()
            when {
                dpStatement.madeWithObjectAsString     -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsStringData))
                dpStatement.madeWithObjectAsDouble    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsDoubleData))
                dpStatement.madeWithObjectAsBoolean    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsBooleanData))
                dpStatement.madeWithObjectAsTimestamp  -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsTimestampData))
                else -> Log.debug("==Error==> ","Unable to delete. Something wrong with DataPropertyStatement.")
            }
            writeSemantic()
        }
    }

    //To save
    /**
     * To save the manipulations done to the ontology
     */
    fun saveOnto(ontologyFilePath: String) {
        this.ontoRef.saveOntology(ontologyFilePath)
    }

    //GET for getting basic ontology information
    /**
     * Get ontology reference
     */
    fun getOntoRef(): OWLReferences {

        return ontoRef
    }

    /**
     * Get temporal ontology reference
     */
    fun getTemporalOntoRef(): OWLReferences {

        return temporalOntoRef
    }

    /**
     * Get ontology file path
     */
    fun getOntoFilePath(): String {

        return ontoFilePath
    }
}
