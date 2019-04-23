import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Observable

class VocalInterfaceEvent {

    fun activateEvent(inferenceFromOnto: String, userNode: String, firebaseDBRef: FirebaseDatabase) {

        when (inferenceFromOnto) {
            "HavingBreakfast" -> firebaseDBRef.getReference("events").child("drugReminderFullStomach").setValueAsync(true)
        }
    }
}