import it.emarolab.amor.owlInterface.OWLReferences
import it.emarolab.amor.owlInterface.OWLReferencesInterface
import it.emarolab.owloop.aMORDescriptor.utility.individual.MORFullIndividual
import org.apache.jena.atlas.logging.Log
import org.semanticweb.owlapi.model.OWLLiteral
import org.semanticweb.owlapi.model.OWLNamedIndividual


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

    //CREATE statements
    /**
     * Creates individual(subject in the statement) & individual(object in the statement) & objectProperty(verb in the statement), in the ontology.
     */
    fun createStatement(statement: ObjectPropertyStatement) {

        if (statement.madeOfSpecialOntoRef) {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual , statement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                addObject(statement.ontoRefForVerb.getOWLObjectProperty(statement.verbAsOwlProperty), statement.ontoRefForObject.getOWLIndividual(statement.objectAsString))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual, getOntoRef())
            individual.apply {
                readSemantic()
                addObject(getOntoRef().getOWLObjectProperty(statement.verbAsOwlProperty), getOntoRef().getOWLIndividual(statement.objectAsString))
                writeSemantic()
            }
        }
    }
    /**
     * Creates individual(subject in the statement) & data(object in the statement) & dataProperty(verb in the statement), in the ontology.
     */
    fun createStatement(statement: DataPropertyStatement) {

        val individual = MORFullIndividual(statement.subjectAsOwlIndividual ,this.ontoRef)
        individual.apply {
            readSemantic()
            when {
                statement.madeWithObjectAsString     -> addData(statement.verbAsOwlProperty, statement.objectAsString)
                statement.madeWithObjectAsTimestamp  -> addData(statement.verbAsOwlProperty, statement.objectAsTimestamp.toString())
                statement.madeWithObjectAsBoolean    -> addData(statement.verbAsOwlProperty, statement.objectAsBoolean,true)
                statement.madeWithObjectAsInteger    -> addData(statement.verbAsOwlProperty, statement.objectAsInteger)
                else -> Log.debug("==Error==> ","IncompleteStatement not correctly created or initialized.")
            }
            writeSemantic()
        }
    }

    //READ inference from statements
    /**
     *  Read Inference of an ObjectPropertyStatement
     */
    fun readInference(opStatement: ObjectPropertyStatement): String {

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
     *  Read Inference of an DataPropertyStatement
     */
    fun readInference(dpStatement: DataPropertyStatement): String {
        val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual , this.ontoRef)
        lateinit var data: OWLLiteral
        individual.apply {
            readSemantic()
            data = individual.dataSemantics.getLiteral(individual.getOWLDataProperty(dpStatement.verbAsOwlProperty))
        }

        return data.literal
    }

//    /**
//     *  Read Inference of an <code>IncompleteStatement</code> whose verb is an ObjectProperty
//     */
//    fun readInferenceObjectPropertyStatement(incompleteStatement: IncompleteStatement): String {
//        lateinit var individual: MORFullIndividual
//        lateinit var namedIndiv: OWLNamedIndividual
//
//        if (incompleteStatement.madeOfSpecialOntoRef) {
//            individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, incompleteStatement.ontoRefForSubject)
//            individual.apply {
//                readSemantic()
//                namedIndiv = individual.getObject(incompleteStatement.ontoRefForVerb.getOWLObjectProperty(incompleteStatement.verbAsOwlProperty))
//            }
//        } else {
//            individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, this.ontoRef)
//            individual.apply {
//                readSemantic()
//                namedIndiv = individual.getObject(incompleteStatement.verbAsOwlProperty)
//            }
//        }
//
//        return individual.getOWLName(namedIndiv)
//    }
//    /**
//     *  Read Inference of an <code>IncompleteStatement</code> whose verb is a DataProperty
//     */
//    fun readInferenceDataPropertyStatement(incompleteStatement: IncompleteStatement): String {
//        val individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual , this.ontoRef)
//        lateinit var data: OWLLiteral
//        individual.apply {
//            readSemantic()
//            data = individual.dataSemantics.getLiteral(individual.getOWLDataProperty(incompleteStatement.verbAsOwlProperty))
//        }
//        return data.literal
//    }

    //UPDATE statements
    /**
     *  Create or update object property opStatement.
     *  If the opStatement does not exist in the ontology, it gets created.
     *  Or if it exists, it gets updated.
     */
    fun createOrUpdateStatement(opStatement: ObjectPropertyStatement, withReplacement: Boolean) {

        if (opStatement.madeOfSpecialOntoRef) {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual , opStatement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                try {
                    val inference = readInference(opStatement)
                    if (inference.contentEquals(opStatement.getObject())) {
                        //The statement's inference is the same, nothing to update.
                    } else {
                        if (withReplacement) {
                            //The statement's inference is not the same, so update with replacement.
                            //(If the inference holds more than one result, then the previous-last one gets replaced)
                            deleteObject(inference,opStatement.ontoRefForObject)
                            addObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsString))
                        } else {
                            //The statement's inference is not the same, so update without replacement.
                            addObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsString))
                        }
                    }
                    addObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsString))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    //When the result of readInference is 'null' we jump to catch
                    addObject(ontoRef.getOWLObjectProperty(opStatement.verbAsOwlProperty), ontoRef.getOWLIndividual(opStatement.objectAsString))
                    writeSemantic()
                }
            }
        } else {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, this.ontoRef)
            individual.apply {
                readSemantic()
                try {
                    val inference = readInference(opStatement)
                    if (inference.contentEquals(opStatement.getObject())) {
                        //The statement's inference is the same, nothing to update.
                    } else {
                        if (withReplacement) {
                            //The statement's inference is not the same, so update with replacement.
                            deleteObject(inference,getOntoRef())
                            addObject(getOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), getOntoRef().getOWLIndividual(opStatement.objectAsString))
                        } else {
                            //The statement's inference is not the same, so update without replacement.
                            addObject(getOntoRef().getOWLObjectProperty(opStatement.verbAsOwlProperty), getOntoRef().getOWLIndividual(opStatement.objectAsString))
                        }
                    }
                    addObject(ontoRef.getOWLObjectProperty(opStatement.verbAsOwlProperty), ontoRef.getOWLIndividual(opStatement.objectAsString))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    //When the result of readInference is 'null' we jump to catch
                    addObject(ontoRef.getOWLObjectProperty(opStatement.verbAsOwlProperty), ontoRef.getOWLIndividual(opStatement.objectAsString))
                    writeSemantic()
                }
            }
        }
    }
    /**
     *  Create or update data property dpStatement.
     *  If the dpStatement does not exist in the ontology, it gets created.
     *  Or if it exists, it gets updated.
     */
    fun createOrUpdateStatement(dpStatement: DataPropertyStatement) {

        val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual ,this.ontoRef)
        individual.apply {
            readSemantic()
            removeData(dpStatement.verbAsOwlProperty)
            when {
                dpStatement.madeWithObjectAsString     -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsString)
                dpStatement.madeWithObjectAsTimestamp  -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsTimestamp.toString())
                dpStatement.madeWithObjectAsBoolean    -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsBoolean,true)
                dpStatement.madeWithObjectAsInteger    -> addData(dpStatement.verbAsOwlProperty, dpStatement.objectAsInteger)
                else -> Log.debug("==Error==> ","IncompleteStatement not correctly created or initialized.")
            }
            writeSemantic()
        }
    }

    //BREAK statement
    /**
     * Works for <code>ObjectPropertyStatement</code>.
     * Simply breaks the relationship between the subject and the object.
     * Note that the subject, verb and object still exist in the ontology.
     */
    fun breakStatement(opStatement: ObjectPropertyStatement) {

        if (opStatement.madeOfSpecialOntoRef) {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual , opStatement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                removeObject(opStatement.ontoRefForVerb.getOWLObjectProperty(opStatement.verbAsOwlProperty), opStatement.ontoRefForObject.getOWLIndividual(opStatement.objectAsString))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(opStatement.subjectAsOwlIndividual, this.ontoRef)
            individual.apply {
                readSemantic()
                removeObject(opStatement.verbAsOwlProperty, opStatement.objectAsString)
                writeSemantic()
            }
        }
    }

    //DELETE statements

    fun deleteSubject(opStatement: ObjectPropertyStatement) {

        if (opStatement.isMadeOfSpecialOntoRef()) {
            opStatement.getSpecialSubjectOntoRef().removeIndividual(opStatement.subjectAsOwlIndividual)
        } else {
            getOntoRef().removeIndividual(opStatement.subjectAsOwlIndividual)
        }
    }

    fun deleteSubject(dpStatement: DataPropertyStatement) {

        if (dpStatement.isMadeOfSpecialOntoRef()) {
            dpStatement.getSpecialSubjectOntoRef().removeIndividual(dpStatement.subjectAsOwlIndividual)
        } else {
            getOntoRef().removeIndividual(dpStatement.subjectAsOwlIndividual)
        }
    }

    fun deleteVerbObjectProperty(opStatement: ObjectPropertyStatement) {
        //For now not useful
    }

    fun deleteVerbDataProperty(dpStatement: DataPropertyStatement) {
        //For now not useful
    }

    /**
     * Deletes the object (object, i.e., individual in the ontology)
     * A redundant second parameter is there for the designer to be conscious about the reference (ontoRef) of the individual in the ontology.
     */
    fun deleteObject(name: String, ontoRef: OWLReferences) {

        ontoRef.removeIndividual(name)
    }
    /**
     * Deletes the object (object, i.e., individual in the ontology)
     * A redundant second parameter is there for the designer to be conscious about the reference (ontoRef) of the individual in the ontology.
     */
    fun deleteObject(opStatement: ObjectPropertyStatement) {

        if (opStatement.isMadeOfSpecialOntoRef()) {
            opStatement.getSpecialObjectOntoRef().removeIndividual(opStatement.objectAsString)
        } else {
            getOntoRef().removeIndividual(opStatement.objectAsString)
        }
    }
    /**
     * Breaks the dataPropertyStatement & deletes the object (object, i.e., data: string, bool or int)
     *
     * Note: feature of deleting dpStatement with particular ontoRefs not added to this function yet. Add if needed.
     */
    fun deleteObject(dpStatement: DataPropertyStatement) {

        val individual = MORFullIndividual(dpStatement.subjectAsOwlIndividual , this.ontoRef)
        individual.apply {
            readSemantic()
            when {
                dpStatement.madeWithObjectAsString     -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsString))
                dpStatement.madeWithObjectAsInteger    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsInteger))
                dpStatement.madeWithObjectAsBoolean    -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsBoolean))
                dpStatement.madeWithObjectAsTimestamp  -> removeData(getOntoRef().getOWLDataProperty(dpStatement.verbAsOwlProperty), getOntoRef().getOWLLiteral(dpStatement.objectAsTimestamp))
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
