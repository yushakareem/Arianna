import it.emarolab.amor.owlInterface.OWLReferences
import it.emarolab.amor.owlInterface.OWLReferencesInterface
import it.emarolab.owloop.aMORDescriptor.utility.individual.MORFullIndividual
import org.apache.jena.atlas.logging.Log
import org.semanticweb.owlapi.model.OWLLiteral
import org.semanticweb.owlapi.model.OWLNamedIndividual


/**
 *
 * @param ontoRefName is the reference name that you would like to give to this ontology.
 * @param ontoFilePath is the location (path) to the .owl file.
 * @param ontoIRI is the Internationalized Resource Identifier which can be found from Protege.
 * @param bufferingReasoner if true, causes the reasoner to take into consideration the changes in the current root ontology.
 */
class Ontology(ontoRefName: String, ontoFilePath: String, ontoIRI: String, bufferingReasoner: Boolean) {

    val temporalOntoRef: OWLReferences
    val notTemporalOntoRef: OWLReferences

    init {

        val owlTimeOntoIRI = "http://www.w3.org/2006/time"
        val owlTimeOntoRef = "owlTimeOntoRef"

        notTemporalOntoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(ontoRefName, ontoFilePath, ontoIRI, bufferingReasoner)
        temporalOntoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(owlTimeOntoRef, ontoFilePath, owlTimeOntoIRI, bufferingReasoner)
    }

    //Function for adding an object(individual or data) .. create
    /**
     * Using the provided statement,
     * create dataProperty(verb in the statement) and data(data in the statement) linked to an individual(subject in the statement) in the ontology.
     *
     * Note: feature of creating statement with particular ontoRefs not added to this function yet. Add if needed.
     */
    fun createDataPropertyStatement(statement: OntoStatement) {

        val individual = MORFullIndividual(statement.subjectAsOwlIndividual ,this.notTemporalOntoRef)
        individual.apply {
            readSemantic()
            when {
                statement.constructedWithObjectAsString     -> addData(statement.verbAsOwlProperty, statement.objectAsOwlString)
                statement.constructedWithObjectAsTimestamp  -> addData(statement.verbAsOwlProperty, statement.objectAsOwlTimestampData.toString())
                statement.constructedWithObjectAsBoolean    -> addData(statement.verbAsOwlProperty, statement.objectAsOwlBooleanData,true)
                statement.constructedWithObjectAsInteger    -> addData(statement.verbAsOwlProperty, statement.objectAsOwlIntegerData)
                else -> Log.debug("==Error==> ","Statement not correctly created or initialized.")
            }
            writeSemantic()
        }
    }

    /**
     * Using the provided statement,
     * create objectProperty(verb in the statement) and individual(object in the statement) linked to an individual(subject in the statement) in the ontology.
     */
    fun createObjectPropertyStatement(statement: OntoStatement) {

        if (statement.hasBeenAssignedParticularOntoRefs) {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual , statement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                addObject(statement.ontoRefForVerb.getOWLObjectProperty(statement.verbAsOwlProperty), statement.ontoRefForObject.getOWLIndividual(statement.objectAsOwlString))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual, this.notTemporalOntoRef)
            individual.apply {
                readSemantic()
                addObject(notTemporalOntoRef.getOWLObjectProperty(statement.verbAsOwlProperty), notTemporalOntoRef.getOWLIndividual(statement.objectAsOwlString))
                writeSemantic()
            }
        }
    }

    //READ inference
    /**
     *  Read Inference of an <code>OntoStatement</code> whose verb is a DataProperty
     */
    fun readInferenceDataPropertyStatement(statement: OntoStatement): String {
        val individual = MORFullIndividual(statement.subjectAsOwlIndividual , this.notTemporalOntoRef)
        lateinit var data: OWLLiteral
        individual.apply {
            readSemantic()
            data = individual.dataSemantics.getLiteral(individual.getOWLDataProperty(statement.verbAsOwlProperty))
        }
        return data.literal
    }
    /**
     *  Read Inference of an <code>IncompleteOntoStatement</code> whose verb is a DataProperty
     */
    fun readInferenceDataPropertyStatement(incompleteStatement: IncompleteOntoStatement): String {
        val individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual , this.notTemporalOntoRef)
        lateinit var data: OWLLiteral
        individual.apply {
            readSemantic()
            data = individual.dataSemantics.getLiteral(individual.getOWLDataProperty(incompleteStatement.verbAsOwlProperty))
        }
        return data.literal
    }
    /**
     *  Read Inference of an <code>OntoStatement</code> whose verb is an ObjectProperty
     */
    fun readInferenceObjectPropertyStatement(statement: OntoStatement): String {

        lateinit var individual: MORFullIndividual
        lateinit var namedIndiv: OWLNamedIndividual

        if (statement.hasBeenAssignedParticularOntoRefs) {
            individual = MORFullIndividual(statement.subjectAsOwlIndividual, statement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                namedIndiv = individual.getObject(statement.ontoRefForVerb.getOWLObjectProperty(statement.verbAsOwlProperty))
            }
        } else {
            individual = MORFullIndividual(statement.subjectAsOwlIndividual, this.notTemporalOntoRef)
            individual.apply {
                readSemantic()
                namedIndiv = individual.getObject(statement.verbAsOwlProperty)
            }
        }

        return individual.getOWLName(namedIndiv)
    }
    /**
     *  Read Inference of an <code>IncompleteOntoStatement</code> whose verb is an ObjectProperty
     */
    fun readInferenceObjectPropertyStatement(incompleteStatement: IncompleteOntoStatement): String {
        lateinit var individual: MORFullIndividual
        lateinit var namedIndiv: OWLNamedIndividual

        if (incompleteStatement.hasBeenAssignedParticularOntoRefs) {
            individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, incompleteStatement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                namedIndiv = individual.getObject(incompleteStatement.ontoRefForVerb.getOWLObjectProperty(incompleteStatement.verbAsOwlProperty))
            }
        } else {
            individual = MORFullIndividual(incompleteStatement.subjectAsOwlIndividual, this.notTemporalOntoRef)
            individual.apply {
                readSemantic()
                namedIndiv = individual.getObject(incompleteStatement.verbAsOwlProperty)
            }
        }

        return individual.getOWLName(namedIndiv)
    }

    //UPDATE
    /**
     *  Create or update data property statement.
     *  If the statement does not exist in the ontology, it gets created.
     *  Or if it exists, it gets updated.
     */
    fun createOrUpdateDataPropertyStatement(statement: OntoStatement) {

        val individual = MORFullIndividual(statement.subjectAsOwlIndividual ,this.notTemporalOntoRef)
        individual.apply {
            readSemantic()
            removeData(statement.verbAsOwlProperty)
            when {
                statement.constructedWithObjectAsString     -> addData(statement.verbAsOwlProperty, statement.objectAsOwlString)
                statement.constructedWithObjectAsTimestamp  -> addData(statement.verbAsOwlProperty, statement.objectAsOwlTimestampData.toString())
                statement.constructedWithObjectAsBoolean    -> addData(statement.verbAsOwlProperty, statement.objectAsOwlBooleanData,true)
                statement.constructedWithObjectAsInteger    -> addData(statement.verbAsOwlProperty, statement.objectAsOwlIntegerData)
                else -> Log.debug("==Error==> ","Statement not correctly created or initialized.")
            }
            writeSemantic()
        }
    }
    /**
     *  Create or update object property statement.
     *  If the statement does not exist in the ontology, it gets created.
     *  Or if it exists, it gets updated.
     */
    fun createOrUpdateObjectPropertyStatement(statement: OntoStatement) {

        if (statement.hasBeenAssignedParticularOntoRefs) {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual , statement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                try {
                    val inferenceOfStatement = readInferenceObjectPropertyStatement(statement)
                    deleteObject(inferenceOfStatement, statement.ontoRefForObject)
                    addObject(statement.ontoRefForVerb.getOWLObjectProperty(statement.verbAsOwlProperty), statement.ontoRefForObject.getOWLIndividual(statement.objectAsOwlString))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    addObject(statement.ontoRefForVerb.getOWLObjectProperty(statement.verbAsOwlProperty), statement.ontoRefForObject.getOWLIndividual(statement.objectAsOwlString))
                    writeSemantic()
                }
            }
        } else {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual, this.notTemporalOntoRef)
            individual.apply {
                readSemantic()
                try {
                    val inferenceOfStatement = readInferenceObjectPropertyStatement(statement)
                    deleteObject(inferenceOfStatement, notTemporalOntoRef)
                    addObject(notTemporalOntoRef.getOWLObjectProperty(statement.verbAsOwlProperty), notTemporalOntoRef.getOWLIndividual(statement.objectAsOwlString))
                    writeSemantic()
                } catch (e: IllegalStateException) {
                    addObject(notTemporalOntoRef.getOWLObjectProperty(statement.verbAsOwlProperty), notTemporalOntoRef.getOWLIndividual(statement.objectAsOwlString))
                    writeSemantic()
                }
            }
        }
    }

    //DELETE statement
    /**
     * Breaks the dataPropertyStatement & deletes the object (object, i.e., data: string, bool or int)
     *
     * Note: feature of deleting statement with particular ontoRefs not added to this function yet. Add if needed.
     */
    fun deleteDataPropertyStatement(statement: OntoStatement) {

        val individual = MORFullIndividual(statement.subjectAsOwlIndividual , this.notTemporalOntoRef)
        individual.apply {
            readSemantic()
            when {
                statement.constructedWithObjectAsString     -> removeData(notTemporalOntoRef.getOWLDataProperty(statement.verbAsOwlProperty), notTemporalOntoRef.getOWLLiteral(statement.objectAsOwlString))
                statement.constructedWithObjectAsInteger    -> removeData(notTemporalOntoRef.getOWLDataProperty(statement.verbAsOwlProperty), notTemporalOntoRef.getOWLLiteral(statement.objectAsOwlIntegerData))
                statement.constructedWithObjectAsBoolean    -> removeData(notTemporalOntoRef.getOWLDataProperty(statement.verbAsOwlProperty), notTemporalOntoRef.getOWLLiteral(statement.objectAsOwlBooleanData))
                statement.constructedWithObjectAsTimestamp  -> removeData(notTemporalOntoRef.getOWLDataProperty(statement.verbAsOwlProperty), notTemporalOntoRef.getOWLLiteral(statement.objectAsOwlTimestampData))
                else -> Log.debug("==Error==> ","Statement not correctly created or initialized.")
            }
            writeSemantic()
        }
    }
    /**
     * Breaks the objectPropertyStatement
     */
    fun deleteObjectPropertyStatement(statement: OntoStatement) {

        if (statement.hasBeenAssignedParticularOntoRefs) {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual , statement.ontoRefForSubject)
            individual.apply {
                readSemantic()
                removeObject(statement.ontoRefForVerb.getOWLObjectProperty(statement.verbAsOwlProperty), statement.ontoRefForObject.getOWLIndividual(statement.objectAsOwlString))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(statement.subjectAsOwlIndividual, this.notTemporalOntoRef)
            individual.apply {
                readSemantic()
                removeObject(statement.verbAsOwlProperty, statement.objectAsOwlString)
                writeSemantic()
            }
        }
    }

    /**
     * Deletes the object (object, i.e., individual in the ontology)
     */
    fun deleteObject(nameOfTheIndividual: String, ontoRefOfIndividual: OWLReferences) {

        ontoRefOfIndividual.removeIndividual(nameOfTheIndividual)
    }

    /**
     * Save the Ontology
     */
    fun saveOnto(placeToSave: String) {

        this.notTemporalOntoRef.saveOntology(placeToSave)
    }
}
