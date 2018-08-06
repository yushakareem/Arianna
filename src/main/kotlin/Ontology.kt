import it.emarolab.amor.owlInterface.OWLReferences
import it.emarolab.amor.owlInterface.OWLReferencesInterface


/**
 *
 * @param ontoRefName is the reference name that you would like to give to this ontology.
 * @param ontoFilePath is the location (path) to the .owl file.
 * @param ontoIRI is the Internationalized Resource Identifier which can be found from Protege.
 * @param bufferingReasoner if true, causes the reasoner to take into consideration the changes in the current root ontology.
 */
class Ontology(ontoRefName: String, ontoFilePath: String, ontoIRI: String, bufferingReasoner: Boolean) {

    val temporalOntoRef: OWLReferences
    val notTemporalOntoRef: OWLReferences

    init {

        val owlTimeOntoIRI = "http://www.w3.org/2006/time"

        notTemporalOntoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(ontoRefName, ontoFilePath, ontoIRI, bufferingReasoner)
        temporalOntoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet(ontoRefName, ontoFilePath, owlTimeOntoIRI, bufferingReasoner)
    }


}

//Self note: while constructing ontologies merge rathar than import. Because if import is used, any changes in the new onto makes changes in the onto being imported.