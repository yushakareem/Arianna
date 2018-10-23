import it.emarolab.amor.owlInterface.OWLReferences
import java.sql.Timestamp

class DataPropertyStatement: IncompleteStatement {

    lateinit var objectAsString: String
    lateinit var objectAsTimestamp: Timestamp
    var objectAsInteger: Int = 0
    var objectAsBoolean: Boolean = false

    lateinit var ontoRefForObject: OWLReferences

    var madeWithObjectAsString: Boolean = false
    var madeWithObjectAsTimestamp: Boolean = false
    var madeWithObjectAsInteger: Boolean = false
    var madeWithObjectAsBoolean: Boolean = false

    /**
     * Constructor can be used when statement's 'verb' is an owlObjectProperty(Individual) or owlDataProperty(Data).
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsString: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsString = objectAsString
        this.madeWithObjectAsString = true
    }

    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty(Data) is a Timestamp.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsTimestampData: Timestamp) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsTimestamp = objectAsTimestampData
        this.madeWithObjectAsTimestamp = true
    }

    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty(Data) is an Integer.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsIntegerData: Int) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsInteger = objectAsIntegerData
        this.madeWithObjectAsInteger = true
    }

    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty(Data) is a Boolean.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsBooleanData: Boolean) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsBoolean = objectAsBooleanData
        this.madeWithObjectAsBoolean = true
    }

    /**
     * Overriding parent method
     * @return ObjectPropertyStatement
     */
     fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences, ontoRefForObject: OWLReferences): DataPropertyStatement {
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

    fun getObjectAsStr(): String {

        return objectAsString
    }

    fun getObjectAsBool(): Boolean {

        return objectAsBoolean
    }

    fun getObjectAsInt(): Int {

        return objectAsInteger
    }

    fun getObjectAsTimestmp(): Timestamp {

        return objectAsTimestamp
    }
}