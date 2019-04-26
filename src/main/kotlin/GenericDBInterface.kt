import com.google.firebase.database.DatabaseReference

interface GenericDBInterface {

    //fun connectToDB(): DatabaseReference

    fun readNodeData(userNode: String, dataNode: String, ontoTaskManager: OntoTaskManager)

//    fun getTimestamp(): Any
//    fun getValue(): Any

//    fun setData(sensorName: String, sensorData: SensorData)

//    fun getBooleanValue(): Boolean
//    fun getIntegerValue(): Int
//    fun getDoubleValue(): Double
//    fun getStringValue(): String

//    fun setBooleanValue(timestamp: Timestamp, booleanValue: Boolean)
//    fun setIntegerValue(timestamp: Timestamp, integerValue: Int)
//    fun setStringValue(timestamp: Timestamp, stringValue: String)
}