import it.emarolab.amor.owlInterface.OWLReferences

/**
 * An ObjectPropertyStatement is made of the structure: 'subject' 'verb' 'object'.
 * Where, 'subject' is an Individual, 'verb' is an ObjectProperty and 'object' is an Individual in the ontology.
 *
 * @return ObjectPropertyStatement
 */
data class ObjectPropertyStatement(var subjectAsOwlIndividual: String, var verbAsOwlProperty: String, var objectAsOwlIndividual: String) {

//    private var objectAsOwlIndividual: String
    private lateinit var specialOntoRefForObject: OWLReferences
    protected lateinit var specialOntoRefForSubject: OWLReferences
    protected lateinit var specialOntoRefForVerb: OWLReferences

    protected var madeOfSpecialOntoRef: Boolean = false

//    /**
//     * Constructor used when the statement's 'verb' is an ObjectProperty and 'object' is an Individual.
//     */
//    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlIndividual: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
//        this.subjectAsOwlIndividual = subjectAsOwlIndividual
//        this.verbAsOwlProperty = verbAsOwlProperty
//        this.objectAsOwlIndividual = objectAsOwlIndividual
//    }

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

    fun hasSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }

    fun getSpecialSubjectOntoRef(): OWLReferences {

        return specialOntoRefForSubject
    }

    fun getSpecialVerbOntoRef(): OWLReferences {

        return specialOntoRefForVerb
    }
    /**
     * Returns special OntoRef of the object in the statement.
     * @return OWLReferences
     */
    fun getSpecialObjectOntoRef(): OWLReferences {

        return specialOntoRefForObject
    }

//    fun getSubject(): String {
//
//        return subjectAsOwlIndividual
//    }
//
//    fun getVerb(): String {
//
//        return verbAsOwlProperty
//    }
//    /**
//     * Returns the object in the statement as a string.
//     * @return String
//     */
//    fun getObject(): String {
//
//        return objectAsOwlIndividual
//    }
//    /**
//     * Compares an [ObjectPropertyStatement] with another [ObjectPropertyStatement].
//     * @return Boolean
//     */
//    fun compare(objectPropertyStatement: ObjectPropertyStatement): Boolean {
//
//        return getSubject() == objectPropertyStatement.getSubject() && getVerb() == objectPropertyStatement.getVerb() && getObject() == objectPropertyStatement.getObject()
//    }
}