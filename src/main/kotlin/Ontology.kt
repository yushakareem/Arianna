import it.emarolab.amor.owlInterface.OWLReferences
import it.emarolab.amor.owlInterface.OWLReferencesInterface
import it.emarolab.owloop.aMORDescriptor.utility.individual.MORFullIndividual
import org.semanticweb.owlapi.model.OWLLiteral
import org.semanticweb.owlapi.model.OWLNamedIndividual

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
    @Volatile lateinit var cleanSubject: String
    @Volatile var cleanVerbList: MutableList<String> = mutableListOf()
    private lateinit var cleanSubjectIndividual: MORFullIndividual

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

        val individual = MORFullIndividual(statement.subject ,this.ontoRef)
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
     *  Infers from Ontology the 'object' of the ObjectPropertyStatement; returns Any.
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
     *  Infers from Ontology the 'object' of the DataPropertyStatement; returns Any.
     */
    fun inferFromOnto(dpStatement: DataPropertyStatement):Any {
        val individual = MORFullIndividual(dpStatement.subject , this.ontoRef)
        lateinit var data: OWLLiteral

        individual.apply {
            readSemantic()
            data = dataSemantics.getLiteral(individual.getOWLDataProperty(dpStatement.verb))
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
//            println("The inference of IncompleteStatement is NULL. Please handle this carefully.")
            opStatement = ObjectPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(),"null") // FUTURE: OPStatement should take ANY
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

        try {
            individual.apply {
                readSemantic()
                data = dataSemantics.getLiteral(individual.getOWLDataProperty(incompleteStatement.getVerb()))
                dpStatement = DataPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(), dataTypeMapper(data))

            }
        } catch (e: IllegalStateException) {
            dpStatement = DataPropertyStatement(incompleteStatement.getSubject(),incompleteStatement.getVerb(), "null")
            //error("The inference of IncompleteStatement is NULL. Please handle this carefully.")
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
     *  Adds (if does not exist already) or Updates (if exists already), the DataPropertyStatement. //By Tommaso: I add with replacement
     */
    fun addOrUpdateToOnto(dpStatement: DataPropertyStatement) = addOrUpdateToOnto(dpStatement, true)
    fun addOrUpdateToOnto(dpStatement: DataPropertyStatement, withReplacement: Boolean) {

        if (dpStatement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(dpStatement.subject, dpStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                removeData(dpStatement.getSpecialVerbOntoRef().getOWLDataProperty(dpStatement.verb))
                addData(dpStatement.getSpecialVerbOntoRef().getOWLDataProperty(dpStatement.verb), dpStatement.getSpecialObjectOntoRef().getOWLLiteral(dpStatement.objectAsAnyData)) // NEW STUFF for adding ANY type into the Ontology
                writeSemantic()
            }
        } else {
            if(withReplacement){
                val individual = MORFullIndividual(dpStatement.subject, this.ontoRef)
                individual.apply {
                    readSemantic()
                    addData(dpStatement.verb, dpStatement.objectAsAnyData,true) // NEW STUFF for adding ANY type into the Ontology
                    writeSemantic()
                }
            }
            else {
                val individual = MORFullIndividual(dpStatement.subject, this.ontoRef)
                individual.apply {
                    readSemantic()
                    removeData(dpStatement.verb)
                    addData(dpStatement.verb, dpStatement.objectAsAnyData) // NEW STUFF for adding ANY type into the Ontology
                    writeSemantic()
                }
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

    /**
     * Breaks the DataPropertyStatement.
     * Means that the subject, verb and object still exist in the ontology but are not related to eachother.   =>BY TOMMY
     */
    /*fun breakStatementInOnto(opStatement: DataPropertyStatement) {  //OLD ONE
        if (opStatement.hasSpecialOntoRef()) {  //Should be tested yet
            val individual = MORFullIndividual(opStatement.subject , opStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                removeData(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.verb).toString(), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.objectAsAnyData.toString()))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(opStatement.subject, this.ontoRef)
            individual.apply {
                readSemantic()
                removeData(opStatement.verb) // Check
                writeSemantic()
            }
        }
    }*/

    fun breakStatementInOnto(opStatement: DataPropertyStatement) {

        if (opStatement.hasSpecialOntoRef()) {

            val individual = MORFullIndividual(opStatement.subject , opStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                //removeObject(opStatement.getSpecialVerbOntoRef().getOWLObjectProperty(opStatement.verb), opStatement.getSpecialObjectOntoRef().getOWLIndividual(opStatement.objectAsAnyData))
                writeSemantic()
            }
        } else {

            val individual = MORFullIndividual(opStatement.subject, this.ontoRef)
            individual.apply {
                readSemantic()
                println(individual)
                removeData(opStatement.verb)
                println(individual)
                writeSemantic()
            }
        }
    }



    fun breakStatementInOnto(icStatement: IncompleteStatement) {

        if (icStatement.hasSpecialOntoRef()) {
            val individual = MORFullIndividual(icStatement.getSubject() , icStatement.getSpecialSubjectOntoRef())
            individual.apply {
                readSemantic()
                removeObject(icStatement.getSpecialSubjectOntoRef().getOWLObjectProperty(icStatement.getSubject()), icStatement.getSpecialVerbOntoRef().getOWLIndividual(icStatement.getVerb()))
                writeSemantic()
            }
        } else {
            val individual = MORFullIndividual(icStatement.getSubject(), this.ontoRef)
            individual.apply {
                readSemantic()
                removeObject(icStatement.getSubject(), icStatement.getVerb())
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
     * Add (if doesn't exist yet) a subject from Ontology.
     */
    fun addSubjectToOnto(name: String, ontoRef: OWLReferences) {
        ontoRef.addIndividual(name)
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
            dpStatement.getSpecialSubjectOntoRef().removeIndividual(dpStatement.subject)
        } else {
            getOntoRef().removeIndividual(dpStatement.subject)
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

        val individual = MORFullIndividual(dpStatement.subject , this.ontoRef)
        individual.apply {
            readSemantic()
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

    fun dataTypeMapper(owlLiteral: OWLLiteral): Any {
        return when {
            owlLiteral.isBoolean -> owlLiteral.literal!!.toBoolean()
            owlLiteral.isDouble -> owlLiteral.literal!!.toDouble()
            owlLiteral.isFloat -> owlLiteral.literal!!.toFloat()
            owlLiteral.isInteger -> owlLiteral.literal!!.toBigInteger()
            owlLiteral.isRDFPlainLiteral -> owlLiteral.literal!!
            else -> owlLiteral.literal!!
        }
    }

    fun cleanWithCondition() {

        if (cleanVerbList.isNotEmpty()) {
            println("cleanVerbList is not empty: ${cleanVerbList.isNotEmpty()}")
            cleanSubjectIndividual = MORFullIndividual(cleanSubject, getOntoRef())
            cleanSubjectIndividual.readSemantic()
            println(cleanSubjectIndividual)

            cleanVerbList.forEach {
                println("\n !! $it !! \n")
                cleanSubjectIndividual.removeData(it)
                println(cleanSubjectIndividual)
            }

            cleanSubjectIndividual.writeSemantic()
            println(cleanSubjectIndividual)
            saveOnto(getOntoRef().filePath)
            cleanVerbList.clear()

            Thread.sleep(10000)
        }
    }

    fun addToCleaner(subject:String, verbList: MutableList<String>) {
        cleanVerbList = verbList
        cleanSubject = subject
    }
}
