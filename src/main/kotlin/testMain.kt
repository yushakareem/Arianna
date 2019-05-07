import com.clarkparsia.pellet.rules.builtins.DateTimeOperators.time
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar

object testMain {

    @JvmStatic
    fun main(args: Array<String>) {

        //  Localization Ontology
        val onto = Ontology(
                "LocalizationOnto",
                "src/main/resources/HumanActivityOntology.owl",
                "http://www.semanticweb.org/Arianna/HumanActivityOntology",
                true
        )

        /** Initialize Firebase DB and Read data from sensors*/
        val fbDB2 = FirebaseConnector("vocalinterface","/Users/tommasaso/Documents/Tesi/IntalliJ/vocalinterface-firebase-adminsdk-3ycvz-8068c39321.json", "/installation_test_name")

        /** Initialize OntoTakManager */

        val ontoTaskManager = OntoTaskManager(onto,fbDB2)

        ontoTaskManager.reasonWithSynchedTime("Instant_CurrentTIme")

        var sia = IncompleteStatement("user","isDoingActivity")
        val isDoingActivity = onto.inferFromOntoToReturnDPStatement(sia)
        println(isDoingActivity)
    }

}