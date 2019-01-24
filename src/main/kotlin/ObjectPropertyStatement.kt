import it.emarolab.amor.owlInterface.OWLReferences

/**
 * An ObjectPropertyStatement is made of the structure: 'subject' 'verb' 'object'.
 * Where, 'subject' is an Individual, 'verb' is an ObjectProperty and 'object' is an Individual in the ontology.
 */
open class ObjectPropertyStatement: IncompleteStatement {

    private var objectAsOwlIndividual: String
    private lateinit var specialOntoRefForObject: OWLReferences

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
     fun assignSpecialOntoRef(specialOntoRefForSubject: OWLReferences, specialOntoRefForVerb: OWLReferences, specialOntoRefForObject: OWLReferences): ObjectPropertyStatement {
        this.specialOntoRefForSubject = specialOntoRefForSubject
        this.specialOntoRefForVerb = specialOntoRefForVerb
        this.specialOntoRefForObject = specialOntoRefForObject
        this.madeOfSpecialOntoRef = true

        return this
    }

    override fun hasSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }

    override fun getSpecialSubjectOntoRef(): OWLReferences {

        return specialOntoRefForSubject
    }

    override fun getSpecialVerbOntoRef(): OWLReferences {

        return specialOntoRefForVerb
    }
    /**
     * Returns special OntoRef of the object in the statement.
     * @return OWLReferences
     */
    fun getSpecialObjectOntoRef(): OWLReferences {

        return specialOntoRefForObject
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