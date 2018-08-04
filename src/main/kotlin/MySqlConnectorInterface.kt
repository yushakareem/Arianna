import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp

interface MySqlConnectorInterface {

    var connection: Connection
    var statement: Statement
    var resultSet: ResultSet?

    var timestamp: Timestamp
    var booleanValue: Boolean
    var integerValue: Int

    fun connectToDBorCreateNewDB(databaseName: String, username: String, password: String)
    fun disconnectFromDB()

    fun readLatestRow(tableName: String): ResultSet
    fun readSpecificRow(tableName: String, rowNumber: Int): ResultSet

    fun getTimestamp(resultSet: ResultSet?): Timestamp
    fun getBooleanValue(resultSet: ResultSet?): Boolean
    fun getIntegerValue(resultSet: ResultSet?): Int

    fun setBooleanValue(tableName: String, timestamp: Timestamp, booleanValue: Boolean)
    fun setIntegerValue(tableName: String, timestamp: Timestamp, integerValue: Int)

    fun getNumberOfRows(tableName: String): Int
}