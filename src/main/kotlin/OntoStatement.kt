import java.sql.Timestamp

/**
 * This class describes the structure of a statement in an ontology (<code>Statement</code>).
 * A full statement is made up of the structure: 'subject' 'verb' 'object', as in English
 * analogous to the structure: 'Individual' 'Object property' 'Individual' OR 'Individual' 'Data property' 'Data(of some type)', in OWL.
 *
 * OntoStatement is (the child class) of IncompleteOntoStatement (the parent class).
 *
 * The idea is,
 * to use <code>IncompleteOntoStatement</code>, when finding inference, and
 * to use <code>OntoStatement</code>, when setting statements in an Onto, checking presence in an Onto, deleting from Onto.
 */

open class IncompleteOntoStatement(var subjectAsOwlIndividual: String, var verbAsOwlProperty: String)

class OntoStatement: IncompleteOntoStatement {
    lateinit var objectAsOwlIndividual: String
    lateinit var objectAsOwlTimestampData: Timestamp
    var objectAsOwlIntegerData: Int = 0
    var objectAsOwlBooleanData: Boolean = false

    /**
     * Constructor can be used when statement's 'verb' is an owlObjectProperty.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlIndividual: String) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlIndividual = objectAsOwlIndividual
    }

    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty when data type is a Timestamp.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlTimestampData: Timestamp) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlTimestampData = objectAsOwlTimestampData
    }
    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty when data type is an Integer.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlIntegerData: Int) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlIntegerData = objectAsOwlIntegerData
    }
    /**
     * Constructor can be used when statement's 'verb' is an owlDataProperty when data type is a Boolean.
     */
    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String, objectAsOwlBooleanData: Boolean) : super(subjectAsOwlIndividual, verbAsOwlProperty) {
        this.objectAsOwlBooleanData = objectAsOwlBooleanData
    }
}