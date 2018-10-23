fun main(args: Array<String>) {


    val PO = Ontology(
            "PO",
            "src/main/resources/WorkingOntos/PrototypeOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/PrototypeOntology",
            true
    )


    val saveNewInSrc = "src/main/resources/WorkingOntos/New.owl"
    val saveNewInDesktop = "~/Desktop/New.owl"

//    val newOnto = Ontology(
//            "New",
//            "src/main/resources/WorkingOntos/New.owl",
//            "http://www.semanticweb.org/emaroLab/YushaKareem/New",
//            true
//    )

    val statement = ObjectPropertyStatement("TI_A", "isReally","Incredible")//.assignSpecialOntoRef(PO.ontoRef,PO.temporalOntoRef,PO.ontoRef)
    //val statement2 = DataPropertyStatement("TI_A", "isReally","Crazy")
    //val incompState = IncompleteStatement("TI_A","before").assignSpecialOntoRef(PO.ontoRef,PO.temporalOntoRef,PO.ontoRef)
    //val particularStatement = ObjectPropertyStatement("TI_A","before","TI_B").assignSpecialOntoRef(PO.ontoRef,PO.temporalOntoRef,PO.ontoRef)


//    val list = listOf("Aeroplane","Bike","crazy","greedy","Humble","Hungry","Light","Poor")
//    list.toObservable()
//            .subscribeBy (
//                onNext = { PO.deleteObject(it,PO.ontoRef) },
//                onError = { it.printStackTrace() },
//                onComplete = { println("Done!") }
//            )


//    PO.createOrUpdateStatement(statement,true)
    PO.deleteVerbObjectProperty(statement)

    PO.saveOnto(PO.getOntoFilePath())
}


