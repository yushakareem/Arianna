interface OntologiesNetwork {

    fun startNetworking(placeOntologyLinks: OntologyLinks, kitchenActOntoLinks: OntologyLinks): Handler {

    }

    interface Handler {

        fun stop()
    }
}