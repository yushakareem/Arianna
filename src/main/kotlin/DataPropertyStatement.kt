import it.emarolab.amor.owlInterface.OWLReferences
import java.sql.Timestamp

/**
 * A DataPropertyStatement is made of the structure: 'subject' 'verb' 'object'.
 * Where, 'subject' is an Individual, 'verb' is an ObjectProperty and 'object' is data in the ontology.
 *
 * @return DataPropertyStatement
 */
class DataPropertyStatement: IncompleteStatement {

    private lateinit var objectAsStringData: String
    private lateinit var objectAsTimestampData: Timestamp
    private var objectAsDoubleData: Double = 0.0
    private var objectAsBooleanData: Boolean = false

    private lateinit var ontoRefForObject: OWLReferences

    private var madeWithObjectAsString: Boolean = false
    private var madeWithObjectAsTimestamp: Boolean = false
    private var madeWithObjectAsDouble: Boolean = false
    private var madeWithObjectAsBoolean: Boolean = false

    /**
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is String data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsStringData: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsStringData = objectAsStringData
        this.madeWithObjectAsString = true
    }
    /**
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Timestamp data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsTimestampData: Timestamp) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsTimestampData = objectAsTimestampData
        this.madeWithObjectAsTimestamp = true
    }
    /**
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Double data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsDoubleData: Double) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsDoubleData = objectAsDoubleData
        this.madeWithObjectAsDouble = true
    }
    /**
     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Boolean data.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsBooleanData: Boolean) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsBooleanData = objectAsBooleanData
        this.madeWithObjectAsBoolean = true
    }
    /**
     * Assign special OntoRef for Subject, Verb, Object.
     * Useful when multiple ontologies are merged in one ontology.
     * For example: (User defined ontology + OWL time) in a single ontology.
     * @return DataPropertyStatement
     */
     fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences, ontoRefForObject: OWLReferences): DataPropertyStatement {
        this.specialOntoRefForSubject = ontoRefForSubject
        this.specialOntoRefForVerb = ontoRefForVerb
        this.ontoRefForObject = ontoRefForObject
        this.madeOfSpecialOntoRef = true

        return this
    }

    override fun hasSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }
    /**
     * Checks if the object is made of string.
     * @return Boolean
     */
    fun hasObjectAsString(): Boolean {
        return madeWithObjectAsString
    }
    /**
     * Checks if the object is made of boolean.
     * @return Boolean
     */
    fun hasObjectAsBoolean(): Boolean {
        return madeWithObjectAsBoolean
    }
    /**
     * Checks if the object is made of timestamp.
     * @return Boolean
     */
    fun hasObjectAsTimestamp(): Boolean {
        return madeWithObjectAsTimestamp
    }
    /**
     * Checks if the object is made of double.
     * @return Boolean
     */
    fun hasObjectAsDouble(): Boolean {
        return madeWithObjectAsDouble
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
    fun getObjectStringData(): String {

        return objectAsStringData
    }
    /**
     * Returns the object in the statement as a Boolean.
     * @return Boolean
     */
    fun getObjectBooleanData(): Boolean {

        return objectAsBooleanData
    }
    /**
     * Returns the object in the statement as an Double.
     * @return Double
     */
    fun getObjectDoubleData(): Double {

        return objectAsDoubleData
    }
    /**
     * Returns the object in the statement as a Timestamp.
     * @return Timestamp
     */
    fun getObjectTimestampData(): Timestamp {

        return objectAsTimestampData
    }
}