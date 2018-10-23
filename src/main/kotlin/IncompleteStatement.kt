import it.emarolab.amor.owlInterface.OWLReferences

/**
 * This class describes the structure of a statement in an ontology (<code>IncompleteStatement</code>).
 * A full statement is made up of the structure: 'subject' 'verb' 'object', as in English
 * analogous to the structure: 'Individual' 'Object property' 'Individual' OR 'Individual' 'Data property' 'Data(of some type)', in OWL.
 *
 * What we consider as a statement is actually a 'declarative statement'.
 * For a clear understanding explore: (Statements) https://philosophy.hku.hk/think/logic/statements.php
 *
 * ObjectPropertyStatement is (the child class) of IncompleteStatement (the parent class).
 *
 * The idea is,
 * to use <code>IncompleteStatement</code>, when finding inference, and
 * to use <code>ObjectPropertyStatement</code>, when setting statements in an Onto, checking presence in an Onto, deleting from Onto.
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
     * Assign special OntoRef for Subject, Verb, Object. Useful when multiple ontologies are merged in one ontology.
     * For example: (User defined ontology + OWL time) in a single ontology
     *
     * @return IncompleteStatement
     */
     fun assignSpecialOntoRef(ontoRefForSubject: OWLReferences, ontoRefForVerb: OWLReferences): IncompleteStatement {
        this.ontoRefForSubject = ontoRefForSubject
        this.ontoRefForVerb = ontoRefForVerb
        this.madeOfSpecialOntoRef = true

        return this
    }

    open fun isMadeOfSpecialOntoRef(): Boolean {

        return madeOfSpecialOntoRef
    }

    open fun getSpecialSubjectOntoRef(): OWLReferences {

        return ontoRefForSubject
    }

    open fun getSpecialVerbOntoRef(): OWLReferences {

        return ontoRefForVerb
    }

    open fun getSubject(): String {

        return subjectAsOwlIndividual
    }

    open fun getVerb(): String {

        return verbAsOwlProperty
    }
}