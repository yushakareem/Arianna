// What we consider as a statement is actually a 'declarative statement'.
// To explore further visit https://philosophy.hku.hk/think/logic/statements.php
import it.emarolab.amor.owlInterface.OWLReferences

/**
 * Developer has to be aware about how an IncompleteStatement is being used.
 *
 * An IncompleteStatement is made of the structure: 'subject' 'verb'.
 * Where, 'subject' is an Individual, 'verb' could be an ObjectProperty or a DataProperty, within the ontology.
 *
 * @return IncompleteStatement
 */

data class IncompleteStatement(private var subject: String, private var verb: String) {

    private lateinit var specialOntoRefForSubject: OWLReferences
    private lateinit var specialOntoRefForVerb: OWLReferences

    private var madeOfSpecialOntoRef: Boolean = false

    /**
     * Assign special OntoRef for Subject, Verb, Object.
     * Useful when multiple ontologies are merged in one ontology.
     * For example: (User defined ontology + OWL time) in a single ontology.
     * @return IncompleteStatement
     */
    fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences): IncompleteStatement {
        this.specialOntoRefForSubject = ontoRefForSubject
        this.specialOntoRefForVerb = ontoRefForVerb
        this.madeOfSpecialOntoRef = true

        return this
    }
    /**
     * Checks if the statement is made of special OntoRef.
     * @return Boolean
     */
    open fun hasSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }
    /**
     * Returns special OntoRef of the subject in the statement.
     * @return OWLReferences
     */
    open fun getSpecialSubjectOntoRef(): OWLReferences {

        return specialOntoRefForSubject
    }
    /**
     * Returns special OntoRef of the verb in the statement.
     * @return OWLReferences
     */
    open fun getSpecialVerbOntoRef(): OWLReferences {

        return specialOntoRefForVerb
    }
    /**
     * Returns the subject in the statement.
     * @return String
     */
    open fun getSubject(): String {

        return subject
    }
    /**
     * Returns the verb in the statement.
     * @return String
     */
    open fun getVerb(): String {

        return verb
    }
}
