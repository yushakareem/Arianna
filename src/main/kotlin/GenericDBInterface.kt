import com.google.firebase.database.DatabaseReference
import io.reactivex.Observable
import java.sql.ResultSet
import java.sql.Timestamp

interface GenericDBInterface {

    fun connectToDB(): DatabaseReference

    fun startReadData(sensorName: String): Observable<SensorData>

    fun getTimestamp(): Any
    fun getValue(): Any

    fun setData(sensorName: String, sensorData: SensorData)

//    fun getBooleanValue(): Boolean
//    fun getIntegerValue(): Int
//    fun getDoubleValue(): Double
//    fun getStringValue(): String

//    fun setBooleanValue(timestamp: Timestamp, booleanValue: Boolean)
//    fun setIntegerValue(timestamp: Timestamp, integerValue: Int)
//    fun setStringValue(timestamp: Timestamp, stringValue: String)
}