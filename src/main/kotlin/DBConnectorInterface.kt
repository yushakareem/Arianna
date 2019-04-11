import java.sql.ResultSet
import java.sql.Timestamp

interface DBConnectorInterface {

    fun connectToDBorCreateNewDB()
    fun disconnectFromDB()

    fun readLatestRow(tableName: String): ResultSet
    fun readSpecificRow(tableName: String, rowNumber: Int): ResultSet

    fun getTimestamp(resultSet: ResultSet?): Timestamp
    fun getBooleanValue(resultSet: ResultSet?): Boolean
    fun getIntegerValue(resultSet: ResultSet?): Int
    fun getDoubleValue(resultSet: ResultSet?): Double
    fun getStringValue(resultSet: ResultSet?): String

    fun setBooleanValue(tableName: String, timestamp: Timestamp, booleanValue: Boolean)
    fun setIntegerValue(tableName: String, timestamp: Timestamp, integerValue: Int)
    fun setStringValue(tableName: String, timestamp: Timestamp, stringValue: String)

    fun getNumberOfRows(tableName: String): Int
    fun getDatatypeOfTheValue(tableName: String): String
}