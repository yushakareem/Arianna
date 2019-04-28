import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.util.*
import kotlin.collections.ArrayList
import org.apache.jena.sparql.function.library.print
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiPredicate
import io.reactivex.subjects.BehaviorSubject
import org.mindswap.pellet.utils.Bool


object testMain {

    @JvmStatic
    fun main(args: Array<String>) {
        var list = mutableListOf("Ciao", "Hello")

//        val todoObservable = Observable.create<String> { emitter -> println("DONE") }
//        val todoObservable = Observable.just(list)
//
//        val disposable = todoObservable.subscribe { t -> System.out.print(t) }

        var test = "Hello!"
        val observableTest = BehaviorSubject.createDefault(test)

        val observable = BehaviorSubject.createDefault(ObjectPropertyStatement("a","b","c"))

        observable.distinctUntilChanged()
                .subscribeBy(
                onNext = { println(">> $it") },
                onError = { println("Some code runs if some error in doingActivity") }
        )
        Thread.sleep(2000)

        test = "test!!"

        observable.onNext(ObjectPropertyStatement("a","b","c"))
        observable.onNext(ObjectPropertyStatement("d","g","l"))
        observable.onNext(ObjectPropertyStatement("a","b","c"))

//
//        var obs = list.toObservable() // extension function for Iterables
//                .filter { it.length >= 5 }
//                .subscribeBy(  // named arguments for lambda Subscribers
//                        onNext = { println(it) },
//                        onError = { it.printStackTrace() },
//                        onComplete = { println("Done!") }
//                )
//
//
//        Thread.sleep(2000)
//
//        obs.addTo(list.toObservable())
    }
//    fun comparer(a:ObjectPropertyStatement,b:ObjectPropertyStatement): Boolean {
//
//        return a.getSubject() == b.getSubject() && a.getVerb() == (b.getVerb())
//    }

}