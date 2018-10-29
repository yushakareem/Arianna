import java.sql.Timestamp
import java.util.concurrent.TimeUnit

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

    val statement = ObjectPropertyStatement("TI_A", "isCrazy","Incredible")//.assignSpecialOntoRef(PO.ontoRef,PO.temporalOntoRef,PO.ontoRef)
    val incompState = IncompleteStatement("TI_A","before").assignSpecialOntoRef(PO.getOntoRef(),PO.getTemporalOntoRef())
    val dpStatement = DataPropertyStatement("TI_A", "hasTemprature",999.0)
    val incomp = IncompleteStatement("TI_A", "hasTemp")

//    val list = listOf("Aeroplane","Bike","crazy","greedy","Humble","Hungry","Light","Poor")
//    list.toObservable()
//            .subscribeBy (
//                onNext = { PO.removeObjectFromOnto(it,PO.ontoRef) },
//                onError = { it.printStackTrace() },
//                onComplete = { println("Done!") }
//            )

//    PO.addOrUpdateToOnto(statement,true)
//    PO.addOrUpdateToOnto(dpStatement)

//    val a = PO.readInferenceDataPropertyStatement(incomp)
//    println("======CHECK====> $a")

    PO.saveOnto(PO.getOntoFilePath())
}


//    val a = "1"
//    println("====== CurrentTime: $a")
//
//    var check = a.matches("(\\d+\\-\\d+\\-\\d+\\ \\d+\\:\\d+\\:\\d+\\.\\d+)?".toRegex())
//    println("====== It is timestamp style: $check")


//val a = PO.inferFromOntoToReturnDPStatement(incomp)
//val s = a.getSubject()
//val v = a.getVerb()
//print(a.getObjectBooleanData())
//
//println("$s, $v, ")
//when {
//    a.isObjectAsBoolean() -> {
//        print(a.getObjectBooleanData())
//        println("====1")
//    }
//    a.isObjectAsTimestamp() -> {
//        print(a.getObjectTimestampData())
//        println("====2")
//    }
//    a.isObjectAsDouble() -> {
//        print(a.getObjectDoubleData())
//        println("====3")
//    }
//    a.isObjectAsString() -> {
//        print(a.getObjectStringData())
//        println("====4")
//    }
//}