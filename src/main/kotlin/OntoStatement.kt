import it.emarolab.amor.owlInterface.OWLReferences
import java.sql.Timestamp

/**
 * This class describes the structure of a statement in an ontology (<code>Statement</code>).
 * A full statement is made up of the structure: 'subject' 'verb' 'object', as in English
 * analogous to the structure: 'Individual' 'Object property' 'Individual' OR 'Individual' 'Data property' 'Data(of some type)', in OWL.
 *
 * What we consider as a statement is actually a 'declarative statement'.
 * For a clear understanding explore: (Statements) https://philosophy.hku.hk/think/logic/statements.php
 *
 * OntoStatement is (the child class) of IncompleteOntoStatement (the parent class).
 *
 * The idea is,
 * to use <code>IncompleteOntoStatement</code>, when finding inference, and
 * to use <code>OntoStatement</code>, when setting statements in an Onto, checking presence in an Onto, deleting from Onto.
 */

open class IncompleteOntoStatement() {

    lateinit var subjectAsOwlIndividual: String
    lateinit var verbAsOwlProperty: String

    lateinit var ontoRefForSubject: OWLReferences
    lateinit var ontoRefForVerb: OWLReferences
    lateinit var ontoRefForObject: OWLReferences

    var hasBeenAssignedParticularOntoRefs: Boolean = false

    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String) : this() {
        this.subjectAsOwlIndividual = subjectAsOwlIndividual
        this.verbAsOwlProperty = verbAsOwlProperty
    }

    /**
     * Assign special OntoRef for Subject, Verb, Object. Useful when multiple ontologies are merged in one ontology.
     * For example: (User defined ontology + OWL time) in a single ontology
     *
     * @return IncompleteOntoStatement
     */
    open fun assignParticularOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences, ontoRefForObject: OWLReferences): IncompleteOntoStatement {
        this.ontoRefForSubject = ontoRefForSubject
        this.ontoRefForVerb = ontoRefForVerb
        this.ontoRefForObject = ontoRefForObject
        this.hasBeenAssignedParticularOntoRefs = true

        return this
    }
}

class OntoStatement: IncompleteOntoStatement {

    lateinit var objectAsOwlString: String
    lateinit var objectAsOwlTimestampData: Timestamp
    var objectAsOwlIntegerData: Int = 0
    var objectAsOwlBooleanData: Boolean = false

    var constructedWithObjectAsString: Boolean = false
    var constructedWithObjectAsTimestamp: Boolean = false
    var constructedWithObjectAsInteger: Boolean = false
    var constructedWithObjectAsBoolean: Boolean = false

    /**
     * Constructor can be used when statement's 'verb' is an owlObjectProperty(Individual) or owlDataProperty(Data).
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlString: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlString = objectAsOwlString
        this.constructedWithObjectAsString = true
    }

    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty(Data) is a Timestamp.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlTimestampData: Timestamp) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlTimestampData = objectAsOwlTimestampData
        this.constructedWithObjectAsTimestamp = true
    }
    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty(Data) is an Integer.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlIntegerData: Int) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlIntegerData = objectAsOwlIntegerData
        this.constructedWithObjectAsInteger = true
    }
    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty(Data) is a Boolean.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlBooleanData: Boolean) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlBooleanData = objectAsOwlBooleanData
        this.constructedWithObjectAsBoolean = true
    }
    /**
     * Overriding parent method
     *
     * @return OntoStatement
     */
    override fun assignParticularOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences, ontoRefForObject: OWLReferences): OntoStatement {
        this.ontoRefForSubject = ontoRefForSubject
        this.ontoRefForVerb = ontoRefForVerb
        this.ontoRefForObject = ontoRefForObject
        this.hasBeenAssignedParticularOntoRefs = true

        return this
    }
}