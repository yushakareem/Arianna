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
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual , statement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                addObject(statement.getSpecialVerbOntoRef().getOWLObjectProperty(statement.verbAsOwlProperty), statement.getSpecialObjectOntoRef().getOWLIndividual(statement.objectAsOwlIndividual))
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
//            when {
//                statement.hasObjectAsString()     -> addData(statement.getVerb(), statement.getObjectStringData())
//                statement.hasObjectAsTimestamp()  -> addData(statement.getVerb(), statement.getObjectTimestampData().toString())
//                statement.hasObjectAsBoolean()    -> addData(statement.getVerb(), statement.getObjectBooleanData(),true)
//                statement.hasObjectAsDouble()    -> addData(statement.getVerb(), statement.getObjectDoubleData())
//                else -> Log.debug("==Error==> ","IncompleteStatement not correctly created or initialized.")
//            }
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
            individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, opStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                namedIndiv = getObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty))
            }
        }else{
            individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, this.ontoRef)
            individual.apply {
                readSemantic()
                namedIndiv = getObject(opStatement.verbAsOwlProperty)
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
            data = dataSemantics.getLiteral(individual.getOWLDataProperty(dpStatement.verbAsOwlProperty))
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
        var opStatement: ObjectPropertyStatement

        try {
            if (incompleteStatement.hasSpecialOntoRef()) {
                individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, incompleteStatement.getSpecialSubjectOntoRef())
                individual.apply {
                    readSemantic()
                    namedIndiv = getObject(incompleteStatement.getSpecialVerbOntoRef().getOWLObjectProperty(incompleteStatement.verbAsOwlProperty))
                }
                inferredObjectAsOWLIndividual = individual.getOWLName(namedIndiv)
                opStatement = ObjectPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty,inferredObjectAsOWLIndividual).assignSpecialOntoRef(incompleteStatement.getSpecialSubjectOntoRef(),incompleteStatement.getSpecialVerbOntoRef(),getOntoRef())
            } else {
                individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, this.ontoRef)
                individual.apply {
                    readSemantic()
                    namedIndiv = getObject(incompleteStatement.verbAsOwlProperty)
                }
                inferredObjectAsOWLIndividual = individual.getOWLName(namedIndiv)
                opStatement = ObjectPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty,inferredObjectAsOWLIndividual)
            }
        } catch (e: IllegalStateException) {
//            println("The inference of IncompleteStatement is NULL. Please handle this carefully.")
            opStatement = ObjectPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty,"null") // FUTURE: OPStatement should take ANY
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
                data = dataSemantics.getLiteral(individual.getOWLDataProperty(incompleteStatement.verbAsOwlProperty))
                inferredString = data.literal

                dataAsSting = inferredString
                dataAsDouble = inferredString.toDoubleOrNull()

                when {
                    inferredString.matches("(\\d+\\-\\d+\\-\\d+\\ \\d+\\:\\d+\\:\\d+\\.\\d+)?".toRegex()) -> {
                        //data is of type Timestamp
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS")
                        val parsedDate = dateFormat.parse(inferredString)
                        dataAsTimestamp = Timestamp(parsedDate.time)
                        dpStatement = DataPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty,dataAsTimestamp)
                    }
                    inferredString in booleanList -> {
                        //data is of type Boolean
                        dataAsBoolean = inferredString.toBoolean()
                        dpStatement = DataPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty,dataAsBoolean)
                    }
                    dataAsDouble != null -> {
                        //data is of type Double
                        dpStatement = DataPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty, dataAsDouble!!)
                    }
                    else -> {
                        //data is of type String
                        dpStatement = DataPropertyStatement(incompleteStatement.subjectAsOwlIndividual,incompleteStatement.verbAsOwlProperty, dataAsSting)
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
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual , opStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                try {
                    val inference = inferFromOnto(opStatement)
                    if (inference.contentEquals(opStatement.objectAsOwlIndividual)) {
                        //The statement's inference is the same, nothing to update.
                    } else {
                        if (withReplacement) {
                            //The statement's inference is not the same, so update with replacement.
                            //(If the inference holds more than one result, then the previous-last one gets replaced)
                            removeObjectFromOnto(inference,opStatement.getSpecialObjectOntoRef())
                            addObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.objectAsOwlIndividual))
                        } else {
                            //The statement's inference is not the same, so update without replacement.
                            addObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.objectAsOwlIndividual))
                        }
                    }
                    addObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.objectAsOwlIndividual))
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
                    if (inference.contentEquals(opStatement.objectAsOwlIndividual)) {
                        //The statement's inference is the same, nothing to update.
                    } else {
                        if (withReplacement) {
                            //The statement's inference is not the same, so update with replacement.
                            //removeObjectFromOnto(inference,getOntoRef())
                            addObject(getOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), getOntoRef().getOWLIndividual(opStatement.objectAsOwlIndividual),true)
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

        if (dpStatement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual, dpStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                removeData(dpStatement.getSpecialVerbOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty))
                addData(dpStatement.getSpecialVerbOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), dpStatement.getSpecialObjectOntoRef().getOWLLiteral(dpStatement.objectAsAnyData)) // NEW STUFF for adding ANY type into the Ontology
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual, this.ontoRef)
            individual.apply {
                readSemantic()
                removeData(dpStatement.verbAsOwlProperty)
                addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsAnyData) // NEW STUFF for adding ANY type into the Ontology
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

    /**
     * Breaks the ObjectPropertyStatement.
     * Means that the subject, verb and object still exist in the ontology but are not related to eachother.
     */
    fun breakStatementInOnto(opStatement: ObjectPropertyStatement) {

        if (opStatement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual , opStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                removeObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.objectAsOwlIndividual))
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

        if (opStatement.hasSpecialOntoRef()) {
            opStatement.getSpecialSubjectOntoRef().removeIndividual(opStatement.subjectAsOwlIndividual)
        } else {
            getOntoRef().removeIndividual(opStatement.subjectAsOwlIndividual)
        }
    }
    /**
     * Removes subject from Ontology.
     */
    fun removeSubjectFromOnto(dpStatement: DataPropertyStatement) {

        if (dpStatement.hasSpecialOntoRef()) {
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

        if (opStatement.hasSpecialOntoRef()) {
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
//            when {
//                dpStatement.hasObjectAsString()     -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectStringData()))
//                dpStatement.hasObjectAsDouble()    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectDoubleData()))
//                dpStatement.hasObjectAsBoolean()    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectBooleanData()))
//                dpStatement.hasObjectAsTimestamp()  -> removeData(getOntoRef().getOWLDataProperty(dpStatement.getVerb()), getOntoRef().getOWLLiteral(dpStatement.getObjectTimestampData()))
//                else -> Log.debug("==Error==> ","Unable to delete. Something wrong with DataPropertyStatement.")
//            }
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
