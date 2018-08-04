import java.sql.Timestamp

/**
 * This class describes the structure of a statement in an ontology (<code>Statement</code>).
 * A full statement is made up of the structure: 'subject' 'verb' 'object', as in English
 * analogous to the structure: 'Individual' 'Object property' 'Individual' OR 'Individual' 'Data property' 'Data(of some type)', in OWL.
 *
 * OntoStatement is a child class of IncompleteOntoStatement (Inheritance)
 *
 * The idea is,
 * to use <code>IncompleteOntoStatements</code>, to find inference from them, and
 * to use <code>OntoStatements</code>, to set statements in an Onto, check presence in an Onto, delete from Onto.
 */

open class IncompleteOntoStatement(var OWLindividualAsSubject: String, var OWLpropertyAsVerb: String)

class OntoStatement: IncompleteOntoStatement {
    lateinit var OWLindividualAsObject: String
    lateinit var OWLtimestampDataAsObject: Timestamp
    var OWLintegerDataataAsObject: Int = 0
    var OWLbooleanDataataAsObject: Boolean = false

    //Can be used for OWL Object-properties
    constructor(OWLindividualAsSubject: String, OWLpropertyAsVerb: String, OWLindividualAsObject: String) : super(OWLindividualAsSubject, OWLpropertyAsVerb)

    //Can be used for OWL Data-properties
    constructor(OWLindividualAsSubject: String, OWLpropertyAsVerb: String, OWLtimestampDataAsObject: Timestamp) : super(OWLindividualAsSubject, OWLpropertyAsVerb)
    constructor(OWLindividualAsSubject: String, OWLpropertyAsVerb: String, OWLintegerDataataAsObject: Int) : super(OWLindividualAsSubject, OWLpropertyAsVerb)
    constructor(OWLindividualAsSubject: String, OWLpropertyAsVerb: String, OWLbooleanDataataAsObject: Boolean) : super(OWLindividualAsSubject, OWLpropertyAsVerb)
}