import it.emarolab.amor.owlInterface.OWLReferences
import it.emarolab.amor.owlInterface.OWLReferencesInterface
import it.emarolab.owloop.aMORDescriptor.utility.individual.MORFullIndividual
import org.apache.jena.atlas.logging.Log
import org.semanticweb.owlapi.model.OWLLiteral
import org.semanticweb.owlapi.model.OWLNamedIndividual
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class helps in initializing ontologies and manipulating them.
 *
 * Please explore these links, for a clear understanding of:
 * (The idea of Ontology based on OWL and SWRL) https://dior.ics.muni.cz/~makub/owl/
 * (Common mistakes while building an ontology) https://protege.stanford.edu/conference/2004/slides/6.1_Horridge_CommonErrorsInOWL.pdf
 *
 * @param ontoRefName is the reference name that you would like to give to this ontology.
 * @param ontoFilePath is the location (path) to the .owl file.
 * @param ontoIRI is the Internationalized Resource Identifier which can be found from Protege.
 * @param bufferingReasoner if true, causes the reasoner to take into consideration the changes in the current root ontology.
 *
 * @return Ontology
 */
class Ontology(private val ontoRefName: String, private val ontoFilePath: String, private val ontoIRI: String, private val bufferingReasoner: Boolean) {

    private val temporalOntoRef: OWLReferences
    private val ontoRef: OWLReferences

    init {

        val owlTimeOntoIRI = "http://www.w3.org/2006/time"
        val owlTimeOntoRef = ontoRefName + "Time"

        ontoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(ontoRefName, ontoFilePath, ontoIRI, bufferingReasoner)
        temporalOntoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(owlTimeOntoRef, ontoFilePath, owlTimeOntoIRI, bufferingReasoner)
    }

    //ADD statements
    /**
     * Adds ObjectPropertyStatement to the Ontology.
     */
    fun addToOnto(statement: ObjectPropertyStatement) {

        if (statement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(statement.getSubject() , statement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                addObject(statement.getSpecialVerbOntoRef().getOWLObjectProperty(statement.getVerb()), statement.getSpecialObjectOntoRef().getOWLIndividual(statement.getObject()))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(statement.getSubject(), getOntoRef())
            individual.apply {
                readSemantic()
                addObject(getOntoRef().getOWLObjectProperty(statement.getVerb()), getOntoRef().getOWLIndividual(statement.getObject()))
                writeSemantic()
            }
        }
    }
    /**
     * Adds DataPropertyStatement to the Ontology.
     */
    fun addToOnto(statement: DataPropertyStatement) {

        val individual = MORFullIndividual(statement.getSubject() ,this.ontoRef)
        individual.apply {
            readSemantic()
            when {
                statement.hasObjectAsString()     -> addData(statement.getVerb(), statement.getObjectStringData())
                statement.hasObjectAsTimestamp()  -> addData(statement.getVerb(), statement.getObjectTimestampData().toString())
                statement.hasObjectAsBoolean()    -> addData(statement.getVerb(), statement.getObjectBooleanData(),true)
                statement.hasObjectAsDouble()    -> addData(statement.getVerb(), statement.getObjectDoubleData())
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

        if (opStatement.hasSpecialOntoRef()) {
            individual = MORFullIndividual(opStatement.getSubject(), opStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                namedIndiv = getObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.getVerb()))
            }
        } else {
            individual = MORFullIndividual(opStatement.getSubject(), this.ontoRef)
            individual.apply {
                readSemantic()
                namedIndiv = getObject(opStatement.getVerb())
            }
        }

        return individual.getOWLName(namedIndiv)
    }
    /**
     *  Infers from Ontology the 'object' of the DataPropertyStatement; returns String.
     */
    fun inferFromOnto(dpStatement: DataPropertyStatement): String {
        val individual = MORFullIndividual(dpStatement.getSubject() , this.ontoRef)
        lateinit var data: OWLLiteral

        individual.apply {
            readSemantic()
            data = dataSemantics.getLiteral(individual.getOWLDataProperty(dpStatement.getVerb()))
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
            if (incompleteStatement.hasSpecialOntoRef()) {
                individual = MORFullIndividual(incompleteStatement.getSubject(), incompleteStatement.getSpecialSubjectOntoRef())
                individual.apply {
                    readSemantic()
                    namedIndiv = getObject(incompleteStatement.getSpecialVerbOntoRef().getOWLObjectProperty(incompleteStatement.getVerb()))
                }
                inferredObjectAsOWLIndividual = individual.getOWLName(namedIndiv)
                opStatement = ObjectPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(),inferredObjectAsOWLIndividual).assignSpecialOntoRef(incompleteStatement.getSpecialSubjectOntoRef(),incompleteStatement.getSpecialVerbOntoRef(),getOntoRef())
            } else {
                individual = MORFullIndividual(incompleteStatement.getSubject(), this.ontoRef)
                individual.apply {
                    readSemantic()
                    namedIndiv = getObject(incompleteStatement.getVerb())
                }
                inferredObjectAsOWLIndividual = individual.getOWLName(namedIndiv)
                opStatement = ObjectPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(),inferredObjectAsOWLIndividual)
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
        val individual = MORFullIndividual(incompleteStatement.getSubject() , this.ontoRef)
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
                data = dataSemantics.getLiteral(individual.getOWLDataProperty(incompleteStatement.getVerb()))
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

        if (opStatement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(opStatement.getSubject() , opStatement.getSpecialSubjectOntoRef())
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
                            removeObjectFromOnto(inference,opStatement.getSpecialObjectOntoRef())
                            addObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.getVerb()), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.getObject()))
                        } else {
                            //The statement's inference is not the same, so update without replacement.
                            addObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.getVerb()), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.getObject()))
                        }
                    }
                    addObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.getVerb()), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.getObject()))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    //When the result of inferFromOnto is 'null' we jump to catch
                    addObject(ontoRef.getOWLObjectProperty(opStatement.getVerb()), ontoRef.getOWLIndividual(opStatement.getObject()))
                    writeSemantic()
                }
            }
        } else {
            val individual = MORFullIndividual(opStatement.getSubject(), this.ontoRef)
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
                            addObject(getOntoRef().getOWLObjectProperty(opStatement.getVerb()), getOntoRef().getOWLIndividual(opStatement.getObject()))
                        } else {
                            //The statement's inference is not the same, so update without replacement.
                            addObject(getOntoRef().getOWLObjectProperty(opStatement.getVerb()), getOntoRef().getOWLIndividual(opStatement.getObject()))
                        }
                    }
                    addObject(ontoRef.getOWLObjectProperty(opStatement.getVerb()), ontoRef.getOWLIndividual(opStatement.getObject()))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    //When the result of inferFromOnto is 'null' we jump to catch
                    addObject(ontoRef.getOWLObjectProperty(opStatement.getVerb()), ontoRef.getOWLIndividual(opStatement.getObject()))
                    writeSemantic()
                }
            }
        }
    }
    /**
     *  Adds (if does not exist already) or Updates (if exists already), the DataPropertyStatement.
     */
    fun addOrUpdateToOnto(dpStatement: DataPropertyStatement) {

        if (dpStatement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(dpStatement.getSubject(), dpStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                removeData(dpStatement.getSpecialVerbOntoRef().getOWLDataProperty(dpStatement.getVerb()))
                addData(dpStatement.getSpecialVerbOntoRef().getOWLDataProperty(dpStatement.getVerb()), dpStatement.getSpecialObjectOntoRef().getOWLLiteral(dpStatement.getObjectAnyData())) // NEW STUFF for adding ANY type into the Ontology
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(dpStatement.getSubject(), this.ontoRef)
            individual.apply {
                readSemantic()
                removeData(dpStatement.getVerb())
                addData(dpStatement.getVerb(), dpStatement.getObjectAnyData()) // NEW STUFF for adding ANY type into the Ontology
//            when {
////                dpStatement.hasObjectAsString()     -> addData(dpStatement.getVerb(), dpStatement.getObjectStringData())
////                dpStatement.hasObjectAsTimestamp()  -> addData(dpStatement.getVerb(), dpStatement.getObjectTimestampData().toString())
////                dpStatement.hasObjectAsBoolean()    -> addData(dpStatement.getVerb(), dpStatement.getObjectBooleanData(),true)
////                dpStatement.hasObjectAsDouble()    -> addData(dpStatement.getVerb(), dpStatement.getObjectDoubleData())
//                else -> Log.debug("==Error==> ","IncompleteStatement not correctly created or initialized.")
//            }
                writeSemantic()
            }
        }
    }

    //BREAK statement
    /**
     * Breaks the ObjectPropertyStatement.
     * Means that the subject, verb and object still exist in the ontology but are not related to eachother.
     */
    fun breakStatementInOnto(opStatement: ObjectPropertyStatement) {

        if (opStatement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(opStatement.getSubject() , opStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                removeObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.getVerb()), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.getObject()))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(opStatement.getSubject(), this.ontoRef)
            individual.apply {
                readSemantic()
                removeObject(opStatement.getVerb(), opStatement.getObject())
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

        if (opStatement.hasSpecialOntoRef()) {
            opStatement.getSpecialSubjectOntoRef().removeIndividual(opStatement.getSubject())
        } else {
            getOntoRef().removeIndividual(opStatement.getSubject())
        }
    }
    /**
     * Removes subject from Ontology.
     */
    fun removeSubjectFromOnto(dpStatement: DataPropertyStatement) {

        if (dpStatement.hasSpecialOntoRef()) {
            dpStatement.getSpecialSubjectOntoRef().removeIndividual(dpStatement.getSubject())
        } else {
            getOntoRef().removeIndividual(dpStatement.getSubject())
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

        if (opStatement.hasSpecialOntoRef()) {
            opStatement.getSpecialObjectOntoRef().removeIndividual(opStatement.getObject())
        } else {
            getOntoRef().removeIndividual(opStatement.getObject())
        }
    }
    /**
     * Removes object from Ontology.
     */
    fun removeObjectFromOnto(dpStatement: DataPropertyStatement) {

        val individual = MORFullIndividual(dpStatement.getSubject() , this.ontoRef)
        individual.apply {
            readSemantic()
            when {
                dpStatement.hasObjectAsString()     -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectStringData()))
                dpStatement.hasObjectAsDouble()    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectDoubleData()))
                dpStatement.hasObjectAsBoolean()    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectBooleanData()))
                dpStatement.hasObjectAsTimestamp()  -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectTimestampData()))
                else -> Log.debug("==Error==> ","Unable to delete. Something wrong with DataPropertyStatement.")
            }
            writeSemantic()
        }
    }

    //Synchronize reasoner
    /**
     * To save the manipulations done to the ontology
     */
    fun synchronizeReasoner() {
        this.ontoRef.synchronizeReasoner()
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
