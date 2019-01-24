import java.sql.ResultSet
import java.sql.Timestamp

interface MySqlConnectorInterface {

    fun connectToDBorCreateNewDB()
    fun disconnectFromDB()

    fun readLatestRow(tableName: String): ResultSet
    fun readSpecificRow(tableName: String, rowNumber: Int): ResultSet

    fun getTimestamp(resultSet: ResultSet?): Timestamp
    fun getBooleanValue(resultSet: ResultSet?): Boolean
    fun getIntegerValue(resultSet: ResultSet?): Int
    fun getStringValue(resultSet: ResultSet?): String

    fun setBooleanValue(tableName: String, timestamp: Timestamp, booleanValue: Boolean)
    fun setIntegerValue(tableName: String, timestamp: Timestamp, integerValue: Int)

    fun getNumberOfRows(tableName: String): Int
}