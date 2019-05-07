import it.emarolab.amor.owlInterface.OWLReferences
import java.sql.Timestamp

/**
 * A DataPropertyStatement is made of the structure: 'subject' 'verb' 'object'.
 * Where, 'subject' is an Individual, 'verb' is a DataProperty and 'object' is data in the ontology.
 *
 * @return DataPropertyStatement
 */
data class DataPropertyStatement(var subject: String, var verb: String, var objectAsAnyData: Any) {

//    private lateinit var objectAsAnyData: Any
//    private lateinit var objectAsStringData: String
//    private lateinit var objectAsTimestampData: Timestamp
//    private var objectAsDoubleData: Double = 0.0
//    private var objectAsBooleanData: Boolean = false

    protected lateinit var specialOntoRefForSubject: OWLReferences
    protected lateinit var specialOntoRefForVerb: OWLReferences

    protected var madeOfSpecialOntoRef: Boolean = false

    private lateinit var ontoRefForObject: OWLReferences

//    private var madeWithObjectAsString: Boolean = false
//    private var madeWithObjectAsTimestamp: Boolean = false
//    private var madeWithObjectAsDouble: Boolean = false
//    private var madeWithObjectAsBoolean: Boolean = false

//    /**
//     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is String data.
//     */
//    constructor(subject: String, verb: String, objectAsStringData: String) : super(subject, verb) {
//        this.objectAsStringData = objectAsStringData
//        this.madeWithObjectAsString = true
//    }
//    /**
//     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Timestamp data.
//     */
//    constructor(subject: String, verb: String, objectAsTimestampData: Timestamp) : super(subject, verb) {
//        this.objectAsTimestampData = objectAsTimestampData
//        this.madeWithObjectAsTimestamp = true
//    }
//    /**
//     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Double data.
//     */
//    constructor(subject: String, verb: String, objectAsDoubleData: Double) : super(subject, verb) {
//        this.objectAsDoubleData = objectAsDoubleData
//        this.madeWithObjectAsDouble = true
//    }
//    /**
//     * Constructor used when the statement's 'verb' is a DataProperty and 'object' is Boolean data.
//     */
//    constructor(subject: String, verb: String, objectAsBooleanData: Boolean) : super(subject, verb) {
//        this.objectAsBooleanData = objectAsBooleanData
//        this.madeWithObjectAsBoolean = true
//    }
    // UNCOMMENT IF THINGS SCREW UP
//    /**
//     * Constructor for type Any.
//     */
//    constructor(subject: String, verb: String, objectAsAnyData: Any) : super(subject, verb) {
//        this.objectAsAnyData = objectAsAnyData
//    }

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

    fun hasSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }
//    /**
//     * Checks if the object is made of string.
//     * @return Boolean
//     */
//    fun hasObjectAsString(): Boolean {
//        return madeWithObjectAsString
//    }
//    /**
//     * Checks if the object is made of boolean.
//     * @return Boolean
//     */
//    fun hasObjectAsBoolean(): Boolean {
//        return madeWithObjectAsBoolean
//    }
//    /**
//     * Checks if the object is made of timestamp.
//     * @return Boolean
//     */
//    fun hasObjectAsTimestamp(): Boolean {
//        return madeWithObjectAsTimestamp
//    }
//    /**
//     * Checks if the object is made of double.
//     * @return Boolean
//     */
//    fun hasObjectAsDouble(): Boolean {
//        return madeWithObjectAsDouble
//    }

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

        return ontoRefForObject
    }

//    fun getSubject(): String {
//
//        return subject
//    }
//
//    fun getVerb(): String {
//
//        return verb
//    }
//    /**
//     * Returns the object in the statement as a String.
//     * @return String
//     */
//    fun getObjectStringData(): String {
//
//        return objectAsStringData
//    }
//    /**
//     * Returns the object in the statement as a Boolean.
//     * @return Boolean
//     */
//    fun getObjectBooleanData(): Boolean {
//
//        return objectAsBooleanData
//    }
//    /**
//     * Returns the object in the statement as an Double.
//     * @return Double
//     */
//    fun getObjectDoubleData(): Double {
//
//        return objectAsDoubleData
//    }
//    /**
//     * Returns the object in the statement as a Timestamp.
//     * @return Timestamp
//     */
//    fun getObjectTimestampData(): Timestamp {
//
//        return objectAsTimestampData
//    }
//
//    /**
//     * Returns ANY type
//     * @return ANY
//     */
//
//    fun getObjectAnyData(): Any {
//
//        return objectAsAnyData
//
//    }
}