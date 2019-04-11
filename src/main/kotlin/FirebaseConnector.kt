import java.sql.ResultSet
import java.sql.Timestamp

class FirebaseConnector(private val databaseName: String, private val username: String, private val password: String): DBConnectorInterface {

    override fun connectToDBorCreateNewDB() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disconnectFromDB() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readLatestRow(tableName: String): ResultSet {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readSpecificRow(tableName: String, rowNumber: Int): ResultSet {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTimestamp(resultSet: ResultSet?): Timestamp {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBooleanValue(resultSet: ResultSet?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIntegerValue(resultSet: ResultSet?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDoubleValue(resultSet: ResultSet?): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStringValue(resultSet: ResultSet?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setBooleanValue(tableName: String, timestamp: Timestamp, booleanValue: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setIntegerValue(tableName: String, timestamp: Timestamp, integerValue: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setStringValue(tableName: String, timestamp: Timestamp, stringValue: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNumberOfRows(tableName: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDatatypeOfTheValue(tableName: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}