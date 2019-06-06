import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.sql.Timestamp
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.collections.*

//class Counter(private val databaseName: String, private val pathToPrivateKey: String, val pathToNode: String) {
class Counter(fbDB2: FirebaseConnector) {
    var realtimeDBRef: DatabaseReference = fbDB2.realtimeDBRef
    var mapAlert: MutableMap<String, Int>
    var milliseconds: Long = 0
    val pathToNode:String = fbDB2.pathToNode

    init {
        val stRef = CounterStorageConnector(fbDB2.storageClient)
        mapAlert = stRef.config()
    }

    fun start(granularityInMinutes: Long) {
        if (granularityInMinutes > 0.toLong()) {
            milliseconds = granularityInMinutes * 60000

            val map = mutableMapOf<String, Timer>()
            realtimeDBRef.child(pathToNode).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach {
                        //println("${it.key}: ${Timestamp(System.currentTimeMillis())}")
                        val timer = Timer("schedule", true)

                        map.put(it.key, timer)
                        onChange(it.key.toString(), map)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error(error)
                }
            })

        } else {
            println("Error: for the counter initialization please set a positive value")
        }
    }

    fun onChange(userId: String, map: MutableMap<String, Timer>) {
        realtimeDBRef.child(pathToNode).child(userId).child("location")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val timer = map.get(userId)
                    val userLoc = dataSnapshot.value


                    timer?.cancel()
                    timer?.purge()

                    realtimeDBRef.child("$pathToNode/$userId/stoppedMinutes").setValueAsync(0)

                    val timerNew = Timer("schedule", true)

                    timerNew.scheduleAtFixedRate(milliseconds, milliseconds) {

                        realtimeDBRef.child(pathToNode).child(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {


                                    val stoppedMinutes = dataSnapshot.child("stoppedMinutes").value.toString().toInt()

                                    val minutes = milliseconds.div(60000)
                                    val temp = stoppedMinutes.plus(minutes)
                                    realtimeDBRef.child("$pathToNode/$userId/stoppedMinutes").setValueAsync(temp)
                                    //println("ID: $userId - Time passed : $temp")

                                    if (temp >= mapAlert["room" + userLoc.toString()]!!) {
                                        val str = "${dataSnapshot.child("name").value} ${dataSnapshot.child("surname").value} has been in the ${locationMapper(userLoc.toString())} for over $temp minutes"
                                        realtimeDBRef.child("$pathToNode/$userId/alert").setValueAsync(str)
                                    }
                                    //println("map = "+map)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    error(error)
                                }

                            })

                    }
                    map.put(userId, timerNew)

                }

                override fun onCancelled(error: DatabaseError) {
                    error(error)
                }
            })
    }

    fun locationMapper(string: String): String{
        /** Mapper to distinguish the user's location */
        lateinit var sensorValue: String
        when (string) {
            "6" -> sensorValue = "Kitchen"
            "5" -> sensorValue = "LivingRoom"
            "4" -> sensorValue = "BathRoom"
            "3" -> sensorValue = "BedRoom"
            "0" -> sensorValue = "Lost"
        }
        return sensorValue
    }

}