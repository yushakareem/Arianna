import it.emarolab.amor.owlInterface.OWLReferences

/**
 * An ObjectPropertyStatement is made up of the structure: 'subject' 'verb' 'object'.
 * Where, 'subject' is an Individual, 'verb' is an ObjectProperty and 'object' is an Individual in the ontology.
 */
open class ObjectPropertyStatement: IncompleteStatement {

    var objectAsOwlIndividual: String
    lateinit var ontoRefForObject: OWLReferences

    /**
     * Constructor used when the statement's 'verb' is an ObjectProperty and 'object' is an Individual.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlIndividual: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.subjectAsOwlIndividual = subjectAsOwlIndividual
        this.verbAsOwlProperty = verbAsOwlProperty
        this.objectAsOwlIndividual = objectAsOwlIndividual
    }
    /**
     * Assign special OntoRef for Subject, Verb, Object.
     * Useful when multiple ontologies are merged in one ontology.
     * For example: (User defined ontology + OWL time) in a single ontology.
     * @return ObjectPropertyStatement
     */
     fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences, ontoRefForObject: OWLReferences): ObjectPropertyStatement {
        this.ontoRefForSubject = ontoRefForSubject
        this.ontoRefForVerb = ontoRefForVerb
        this.ontoRefForObject = ontoRefForObject
        this.madeOfSpecialOntoRef = true

        return this
    }

    override fun isMadeOfSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }

    override fun getSpecialSubjectOntoRef(): OWLReferences {

        return ontoRefForSubject
    }

    override fun getSpecialVerbOntoRef(): OWLReferences {

        return ontoRefForVerb
    }
    /**
     * Returns special OntoRef of the object in the statement.
     * @return OWLReferences
     */
    fun getSpecialObjectOntoRef(): OWLReferences {

        return ontoRefForObject
    }

    override fun getSubject(): String {

        return subjectAsOwlIndividual
    }

    override fun getVerb(): String {

        return verbAsOwlProperty
    }
    /**
     * Returns the object in the statement as a string.
     * @return String
     */
    fun getObject(): String {

        return objectAsOwlIndividual
    }
}