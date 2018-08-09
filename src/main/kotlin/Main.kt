import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import it.emarolab.owloop.aMORDescriptor.utility.individual.MORFullIndividual
import openllet.shared.tools.Log

fun main(args: Array<String>) {

    val PO = Ontology(
            "PO",
            "src/main/resources/WorkingOntos/PrototypeOntology.owl",
            "http://www.semanticweb.org/emaroLab/YushaKareem/PrototypeOntology",
            true
    )

    println(PO.notTemporalOntoRef.useBufferingReasoner())
//    val newOnto = Ontology(
//            "New",
//            "src/main/resources/WorkingOntos/New.owl",
//            "http://www.semanticweb.org/emaroLab/YushaKareem/New",
//            true
//    )

    val statement = OntoStatement("TI_A", "isReally","Crazy")//.assignParticularOntoRef(PO.notTemporalOntoRef,PO.temporalOntoRef,PO.notTemporalOntoRef)
    //val incompState = IncompleteOntoStatement("TI_A","before").assignParticularOntoRef(PO.notTemporalOntoRef,PO.temporalOntoRef,PO.notTemporalOntoRef)
    //val particularStatement = OntoStatement("TI_A","before","TI_B").assignParticularOntoRef(PO.notTemporalOntoRef,PO.temporalOntoRef,PO.notTemporalOntoRef)


//    val list = listOf("Aeroplane","Bike","crazy","greedy","Humble","Hungry","Light","Poor")
//    list.toObservable()
//            .subscribeBy (
//                onNext = { PO.deleteObject(it,PO.notTemporalOntoRef) },
//                onError = { it.printStackTrace() },
//                onComplete = { println("Done!") }
//            )



//    PO.createOrUpdateObjectPropertyStatement(statement)
//    PO.saveOnto("src/main/resources/WorkingOntos/New.owl")
}