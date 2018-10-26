// What we consider as a statement is actually a 'declarative statement'.
// To explore further visit https://philosophy.hku.hk/think/logic/statements.php
import it.emarolab.amor.owlInterface.OWLReferences

/**
 * An IncompleteStatement is made up of the structure: 'subject' 'verb'.
 * Where, 'subject' is an Individual, 'verb' is an ObjectProperty in the ontology.
 */

open class IncompleteStatement() {

    lateinit var subjectAsOwlIndividual: String
    lateinit var verbAsOwlProperty: String

    lateinit var ontoRefForSubject: OWLReferences
    lateinit var ontoRefForVerb: OWLReferences

    var madeOfSpecialOntoRef: Boolean = false

    constructor(subjectAsOwlIndividual: String, verbAsOwlProperty: String) : this() {
        this.subjectAsOwlIndividual = subjectAsOwlIndividual
        this.verbAsOwlProperty = verbAsOwlProperty
    }
    /**
     * Assign special OntoRef for Subject, Verb, Object.
     * Useful when multiple ontologies are merged in one ontology.
     * For example: (User defined ontology + OWL time) in a single ontology.
     * @return IncompleteStatement
     */
     fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences): IncompleteStatement {
        this.ontoRefForSubject = ontoRefForSubject
        this.ontoRefForVerb = ontoRefForVerb
        this.madeOfSpecialOntoRef = true

        return this
    }
    /**
     * Checks if the statement is made of special OntoRef.
     * @return Boolean
     */
    open fun isMadeOfSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }
    /**
     * Returns special OntoRef of the subject in the statement.
     * @return OWLReferences
     */
    open fun getSpecialSubjectOntoRef(): OWLReferences {

        return ontoRefForSubject
    }
    /**
     * Returns special OntoRef of the verb in the statement.
     * @return OWLReferences
     */
    open fun getSpecialVerbOntoRef(): OWLReferences {

        return ontoRefForVerb
    }
    /**
     * Returns the subject in the statement.
     * @return String
     */
    open fun getSubject(): String {

        return subjectAsOwlIndividual
    }
    /**
     * Returns the verb in the statement.
     * @return String
     */
    open fun getVerb(): String {

        return verbAsOwlProperty
    }
}