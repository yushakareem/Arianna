import java.sql.*

/**
 *  This class helps in connecting with a MySQL DB and manipulate it.
 */
class MySqlConnector(private val databaseName: String, private val username: String, private val password: String): MySqlConnectorInterface {

    private lateinit var connection: Connection
    private lateinit var statement: Statement
    private var resultSet: ResultSet? = null
    private lateinit var timestamp: Timestamp
    private var booleanValue: Boolean = false //primitives have to be initialized
    private var integerValue: Int = 0 //primitives have to be initialized
    private var doubleValue: Double = 0.0 //primitives have to be initialized
    private lateinit var stringValue: String

    /**
     * Looks for the JDBC driver, if it exists, then makes connection with the database.
     * If the database does not already exist in the MySQL server then it creates a new one.
     * It initializes SQL 'connection' and 'statement' objects.
     * Do not forget to <code>disconnectFromDB()</code> later (after work done with the DB).
     *
     * @param databaseName The <code>String</code> representing the database name.
     * @param username The <code>String</code> of username of SQL server.
     * @param password The <code>String</code> of password of SQL server.
     */
    override fun connectToDBorCreateNewDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            error("Cannot find jdbc driver.")
        }

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/$databaseName?autoReconnect=true&verifyServerCertificate=false&useSSL=true", username, password)
//            println("DataBase with the name: $databaseName already exists. Making connection and creating statement.")
            try {
                statement = connection.createStatement()
            } catch (e1: SQLException) {
                error("Although DB already exist and connection is made, problem in creating SQL statement object.")
            }
        } catch (e: SQLException) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/?autoReconnect=true&verifyServerCertificate=false&useSSL=true", username, password)
            } catch (e1: SQLException) {
                error("Unable to connect with SQL server, Please check if server is running OR check username and password.")
            }

            try {
                statement = connection.createStatement()
            } catch (e1: SQLException) {
                error("Problem in creating SQL statement object.")
            }

            try {
                statement.executeUpdate("CREATE DATABASE $databaseName")
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/$databaseName?autoReconnect=true&verifyServerCertificate=false&useSSL=true", username, password)
                println("Created DataBase with the name: $databaseName. Making connection now.")
            } catch (e1: SQLException) {
                error("Problem in creating a new DataBase with the name: $databaseName.")
            }

        }

    }

    /**
     * Execute this after all work done is with the DB.
     */
    override fun disconnectFromDB() {
        try {
            resultSet?.close()
        } catch (e: SQLException) {
            error("Problem in closing SQL resultSet.")
        }

        try {
            statement.close()
        } catch (e: SQLException) {
            error("Problem in closing SQL statement.")
        }

        try {
            connection.close()
        } catch (e: SQLException) {
            error("Problem in closing SQL connection.")
        }
    }

    /**
     * Reads the latest row (by making the query) from the table in the database.
     * Dont forget to do ResultSet.next() once you have the result set object.
     *
     * @param tableName The <code>String</code> representing the table in the database.
     *
     * @return resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     */
    override fun readLatestRow(tableName: String): ResultSet {
        val query = "select * from $tableName order by time desc limit 1"
        try {
            resultSet = statement.executeQuery(query)
        } catch (e: SQLException) {
            error("Please check the 'table name' in database.")
        }

        return resultSet!!
    }

    /**
     * Reads a specific row (by making the query) from the table in the database. Rows start from the number 0.
     * Dont forget to do ResultSet.next() once you have the result set object.
     *
     * @param tableName The <code>String</code> representing the table in the database.
     *
     * @return resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     */
    override fun readSpecificRow(tableName: String, rowNumber: Int): ResultSet {
        val query = "SELECT * FROM $tableName limit 1 OFFSET $rowNumber"
        try {
            resultSet = statement.executeQuery(query)
        } catch (e: SQLException) {
            error("Please check the 'table name' or 'row number' in database.")
        }
        return resultSet!!
    }

    /**
     * Returns the timestamp from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return timestamp of <code>Timestamp</code> type.
     */
    override fun getTimestamp(resultSet: ResultSet?): Timestamp {
        try {
            timestamp = resultSet!!.getTimestamp("time")
        } catch (e: SQLException) {
            error("Problem in getting timestamp: Check if you have done resultSet.next()")
        }
        return timestamp
    }

    /**
     * Returns the booleanValue from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return booleanValue of <code>Boolean</code> type.
     */
    override fun getBooleanValue(resultSet: ResultSet?): Boolean {
        try {
            booleanValue = resultSet!!.getBoolean("value")
        } catch (e: SQLException) {
            error("Problem in getting booleanValue: Check if you have done resultSet.next()")
        }
        return booleanValue
    }

    /**
     * Returns the integerValue from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return integerValue of <code>Int</code> type.
     */
    override fun getIntegerValue(resultSet: ResultSet?): Int {
        try {
            integerValue = resultSet!!.getInt("value")
        } catch (e: SQLException) {
            error("Problem in getting integerValue: Check if you have done resultSet.next()")
        }
        return integerValue
    }

    /**
     * Returns the integerValue from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return double
     */
    override fun getDoubleValue(resultSet: ResultSet?): Double {
        try {
            doubleValue = resultSet!!.getDouble("value")
        } catch (e: SQLException) {
            error("Problem in getting integerValue: Check if you have done resultSet.next()")
        }
        return doubleValue
    }

    /**
     * Returns the stringValue from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return stringValue of <code>String</code> type.
     */
    override fun getStringValue(resultSet: ResultSet?): String {
        try {
            stringValue = resultSet!!.getString("value")
        } catch (e: SQLException) {
            error("Problem in getting stringValue: Check if you have done resultSet.next()")
        }
        return stringValue
    }

    /**
     * Allows to set timestamp and booleanValue in the table.
     *
     * @param tableName The <code>String</code> name of the table.
     * @param timestamp The <code>Timestamp</code> to be added to the table.
     * @param booleanValue The <code>Boolean</code> to be added to the table.
     */
    override fun setBooleanValue(tableName: String, timestamp: Timestamp, booleanValue: Boolean) {
        val query = "INSERT INTO $tableName (time,value) VALUES (?,?)"
        try {
            val preparedStmt = connection.prepareStatement(query)
            preparedStmt?.setTimestamp(1, timestamp)
            preparedStmt?.setBoolean(2, booleanValue)
            preparedStmt?.execute()
        } catch (e: SQLException) {
            error("Problem in inserting timestamp and booleanValue in the table: $tableName")
        }
    }

    /**
     * Allows to set timestamp and integerValue in the table.
     *
     * @param tableName The <code>String</code> name of the table.
     * @param timestamp The <code>Timestamp</code> to be added to the table.
     * @param integerValue The <code>Int</code> to be added to the table.
     */
    override fun setIntegerValue(tableName: String, timestamp: Timestamp, integerValue: Int) {
        val query = "INSERT INTO $tableName (time,value) VALUES (?,?)"
        try {
            val preparedStmt = connection.prepareStatement(query)
            preparedStmt?.setTimestamp(1, timestamp)
            preparedStmt?.setInt(2, integerValue)
            preparedStmt?.execute()
        } catch (e: SQLException) {
            error("Problem in inserting timestamp and integerValue in the table: $tableName")
        }
    }

    /**
     * Allows to set timestamp and integerValue in the table.
     *
     * @param tableName The <code>String</code> name of the table.
     * @param timestamp The <code>Timestamp</code> to be added to the table.
     * @param stringValue The <code>String</code> to be added to the table.
     */
    override fun setStringValue(tableName: String, timestamp: Timestamp, stringValue: String) {
        val query = "INSERT INTO $tableName (time,value) VALUES (?,?)"
        try {
            val preparedStmt = connection.prepareStatement(query)
            preparedStmt?.setTimestamp(1, timestamp)
            preparedStmt?.setString(2, stringValue)
            preparedStmt?.execute()
        } catch (e: SQLException) {
            error("Problem in inserting timestamp and stringValue in the table: $tableName")
        }
    }

    /**
     * Counts the number of rows in the table and returns the value.
     *
     * @param tableName The <code>String</code> name of the table.
     *
     * @return numberOfRows of <code>Int</code> type.
     */
    override fun getNumberOfRows(tableName: String): Int {
        val query = "SELECT COUNT(*) AS rows FROM $tableName"
        try {
            resultSet = statement.executeQuery(query)
        } catch (e: SQLException) {
            error("MySQL Making query problem")
        }

        var numberOfRows = 0
        if (resultSet!!.next()) {
            try {
                numberOfRows = resultSet!!.getInt(1)
            } catch (e: SQLException) {
                error("MySQL getting Item TimeStamp/Value problem")
            }
        }
        return numberOfRows
    }

    /**
     * Gets datatype of the field 'value' from the desired table in DB.
     *
     * @param tableName The <code>String</code> name of the table.
     *
     * @return datatype The datatype returned as a <code>String</code>.
     */
    override fun getDatatypeOfTheValue(tableName: String): String {
        val query = "select data_type from information_schema.columns where table_name = '$tableName' and column_name = 'value'"
        try {
            resultSet = statement.executeQuery(query)
        } catch (e: SQLException) {
            error("Please check the 'table name' in database.")
        }

        lateinit var datatype: String
        if (resultSet!!.next()) {
            try {
                datatype = resultSet!!.getString(1)
            } catch (e: SQLException) {
                error("MySQL getting Item TimeStamp/Value problem")
            }
        }

        return datatype
    }
}