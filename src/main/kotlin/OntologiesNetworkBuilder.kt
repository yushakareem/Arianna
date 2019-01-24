/**
 * A network of ontologies is an attempt at computationally-scalable reasoning using formal logic for human activity recognition.
 * Ontologies are based on a system of formal logic called Description-logic. Protege is the editor used to create such ontologies.
 *
 * For a clear understanding of above sentences, explore these links:
 * (Link Arianna+ Paper)
 * (what is Logic) https://philosophy.hku.hk/think/logic/whatislogic.php
 */

class OntologiesNetworkBuilder {

    // Here i would like to enable and disable logging
    // fun withAnalyticsDisabled() {}

    fun build(): OntologiesNetwork {
        return OntologiesNetwork()
    }

}