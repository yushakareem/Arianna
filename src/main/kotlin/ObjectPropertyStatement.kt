import it.emarolab.amor.owlInterface.OWLReferences

open class ObjectPropertyStatement: IncompleteStatement {

    var objectAsString: String
    lateinit var ontoRefForObject: OWLReferences

    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsString: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.subjectAsOwlIndividual = subjectAsOwlIndividual
        this.verbAsOwlProperty = verbAsOwlProperty
        this.objectAsString = objectAsString
    }

    /**
     * @return ObjectPropertyStatement
     */
     fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences, ontoRefForObject: OWLReferences): ObjectPropertyStatement {
        this.ontoRefForSubject = ontoRefForSubject
        this.ontoRefForVerb = ontoRefForVerb
        this.ontoRefForObject = ontoRefForObject
        this.madeOfSpecialOntoRef = true

        return this
    }

    /**
     * Overriding parent method
     * @return Boolean
     */
    override fun isMadeOfSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }

    override fun getSpecialSubjectOntoRef(): OWLReferences {

        return ontoRefForSubject
    }

    override fun getSpecialVerbOntoRef(): OWLReferences {

        return ontoRefForVerb
    }

    fun getSpecialObjectOntoRef(): OWLReferences {

        return ontoRefForObject
    }

    override fun getSubject(): String {

        return subjectAsOwlIndividual
    }

    override fun getVerb(): String {

        return verbAsOwlProperty
    }

    fun getObject(): String {

        return objectAsString
    }
}