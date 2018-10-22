fun main(args: Array<String>) {

    val PO = Ontology(
            "PO",
            "src/main/resources/WorkingOntos/PrototypeOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/PrototypeOntology",
            true
    )

    println(PO.getOntologyRef().useBufferingReasoner())
//    val newOnto = Ontology(
//            "New",
//            "src/main/resources/WorkingOntos/New.owl",
//            "http://www.semanticweb.org/emaroLab/YushaKareem/New",
//            true
//    )

    val statement = OntoStatement("TI_A", "isReally","Crazy")//.assignParticularOntoRef(PO.ontoRef,PO.temporalOntoRef,PO.ontoRef)
    //val incompState = IncompleteOntoStatement("TI_A","before").assignParticularOntoRef(PO.ontoRef,PO.temporalOntoRef,PO.ontoRef)
    //val particularStatement = OntoStatement("TI_A","before","TI_B").assignParticularOntoRef(PO.ontoRef,PO.temporalOntoRef,PO.ontoRef)


//    val list = listOf("Aeroplane","Bike","crazy","greedy","Humble","Hungry","Light","Poor")
//    list.toObservable()
//            .subscribeBy (
//                onNext = { PO.deleteObject(it,PO.ontoRef) },
//                onError = { it.printStackTrace() },
//                onComplete = { println("Done!") }
//            )


//    PO.createOrUpdateObjectPropertyStatement(statement)
//    PO.saveOnto("src/main/resources/WorkingOntos/New.owl")
}


