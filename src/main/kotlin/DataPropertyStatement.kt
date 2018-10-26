import it.emarolab.amor.owlInterface.OWLReferences
import java.sql.Timestamp

/**
 * A DataPropertyStatement is made up of the structure: 'subject' 'verb' 'object'.
 * Where, 'subject' is an Individual, 'verb' is an ObjectProperty and 'object' is data in the ontology.
 */
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
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is String data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsString: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsString = objectAsString
        this.madeWithObjectAsString = true
    }
    /**
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Timestamp data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsTimestampData: Timestamp) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsTimestamp = objectAsTimestampData
        this.madeWithObjectAsTimestamp = true
    }
    /**
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Integer data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsIntegerData: Int) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsInteger = objectAsIntegerData
        this.madeWithObjectAsInteger = true
    }
    /**
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Boolean data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsBooleanData: Boolean) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsBoolean = objectAsBooleanData
        this.madeWithObjectAsBoolean = true
    }
    /**
     * Assign special OntoRef for Subject, Verb, Object.
     * Useful when multiple ontologies are merged in one ontology.
     * For example: (User defined ontology + OWL time) in a single ontology.
     * @return DataPropertyStatement
     */
     fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences, ontoRefForObject: OWLReferences): DataPropertyStatement {
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
     * Returns the object in the statement as a String.
     * @return String
     */
    fun getObjectAsStr(): String {

        return objectAsString
    }
    /**
     * Returns the object in the statement as a Boolean.
     * @return Boolean
     */
    fun getObjectAsBool(): Boolean {

        return objectAsBoolean
    }
    /**
     * Returns the object in the statement as an Integer.
     * @return Int
     */
    fun getObjectAsInt(): Int {

        return objectAsInteger
    }
    /**
     * Returns the object in the statement as a Timestamp.
     * @return Timestamp
     */
    fun getObjectAsTimestmp(): Timestamp {

        return objectAsTimestamp
    }
}