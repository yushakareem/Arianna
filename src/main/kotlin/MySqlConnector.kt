import java.sql.*

/**
 *  This class helps in connecting with a MySQL DB and manipulate it.
 */
class MySqlConnector: MySqlConnectorInterface {
    override lateinit var connection: Connection
    override lateinit var statement: Statement
    override var resultSet: ResultSet? = null
    override lateinit var timestamp: Timestamp
    override var booleanValue: Boolean = false //primitives have to be initialized
    override var integerValue: Int = 0 //primitives have to be initialized

    /**
     * Looks for the JDBC driver, if it exists, then makes connection with the database.
     * If the database does not already exist in the MySQL server then it creates a new one.
     * It initializes SQL 'connection' and 'statement' objects.
     * Do not forget to <code>disconnectFromDB()</code> later (after work done with the DB).
     *
     * @param databaseName  The <code>String</code> representing the database name.
     * @param username  The <code>String</code> of username of SQL server.
     * @param password  The <code>String</code> of password of SQL server.
     */
    override fun connectToDBorCreateNewDB(databaseName: String, username: String, password: String) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            println("Cannot find jdbc driver.")
        }

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/$databaseName?autoReconnect=true&verifyServerCertificate=false&useSSL=true", username, password)
            println("DataBase with the name: $databaseName already exists. Making connection and creating statement.")
            try {
                statement = connection.createStatement()
            } catch (e1: SQLException) {
                println("Although DB already exist and connection is made, problem in creating SQL statement object.")
            }
        } catch (e: SQLException) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/?autoReconnect=true&verifyServerCertificate=false&useSSL=true", username, password)
            } catch (e1: SQLException) {
                println("Unable to connect with SQL server, Please check if server is running OR check username and password.")
            }

            try {
                statement = connection.createStatement()
            } catch (e1: SQLException) {
                println("Problem in creating SQL statement object.")
            }

            try {
                statement.executeUpdate("CREATE DATABASE $databaseName")
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/$databaseName?autoReconnect=true&verifyServerCertificate=false&useSSL=true", username, password)
                println("Created DataBase with the name: $databaseName. Making connection now.")
            } catch (e1: SQLException) {
                println("Problem in creating a new DataBase with the name: $databaseName.")
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
            println("Problem in closing SQL resultSet.")
        }

        try {
            statement.close()
        } catch (e: SQLException) {
            println("Problem in closing SQL statement.")
        }

        try {
            connection.close()
        } catch (e: SQLException) {
            println("Problem in closing SQL connection.")
        }
    }

    /**
     * Reads the latest row (by making the query) from the table in the database.
     * Dont forget to do ResultSet.next() once you have the result set object.
     *
     * @param tableName  The <code>String</code> representing the table in the database.
     *
     * @return resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     */
    override fun readLatestRow(tableName: String): ResultSet {
        try {
            val query = "select * from $tableName order by time desc limit 1"
            resultSet = statement.executeQuery(query)
        } catch (e: SQLException) {
            println("MySQL Making query problem")
        }
        return resultSet!!
    }

    /**
     * Reads a specific row (by making the query) from the table in the database. Rows start from the number 0.
     * Dont forget to do ResultSet.next() once you have the result set object.
     *
     * @param tableName  The <code>String</code> representing the table in the database.
     *
     * @return resultSet The <code>ResultSet Object</code> that holds the data read from the table.
     */
    override fun readSpecificRow(tableName: String, rowNumber: Int): ResultSet {
        val query = "SELECT * FROM $tableName limit 1 OFFSET $rowNumber"
        try {
            resultSet = statement.executeQuery(query)
        } catch (e: SQLException) {
            println("MySQL Making query problem")
        }
        return resultSet!!
    }

    /**
     * Returns the timestamp from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet  The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return timestamp of <code>Timestamp</code> type.
     */
    override fun getTimestamp(resultSet: ResultSet?): Timestamp {
        try {
            timestamp = resultSet!!.getTimestamp("time")
        } catch (e: SQLException) {
            println("Problem in getting timestamp")
        }
        return timestamp
    }

    /**
     * Returns the booleanValue from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet  The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return booleanValue of <code>Boolean</code> type.
     */
    override fun getBooleanValue(resultSet: ResultSet?): Boolean {
        try {
            booleanValue = resultSet!!.getBoolean("value")
        } catch (e: SQLException) {
            println("Problem in getting booleanValue")
        }
        return booleanValue
    }

    /**
     * Returns the integerValue from the resultSet object. Note that the column name of the table (in SQL database)
     * is hardcoded in this method.
     *
     * Attention: this method has good scope for modification.
     *
     * @param resultSet  The <code>ResultSet Object</code> that holds the data read from the table.
     *
     * @return integerValue of <code>Int</code> type.
     */
    override fun getIntegerValue(resultSet: ResultSet?): Int {
        try {
            integerValue = resultSet!!.getInt("value")
        } catch (e: SQLException) {
            println("Problem in getting booleanValue")
        }
        return integerValue
    }

    /**
     * Allows to set timestamp and booleanValue in the table.
     *
     * @param tableName  The <code>String</code> name of the table.
     * @param timestamp  The <code>Timestamp</code> to be added to the table.
     * @param booleanValue  The <code>Boolean</code> to be added to the table.
     */
    override fun setBooleanValue(tableName: String, timestamp: Timestamp, booleanValue: Boolean) {
        val query = "INSERT INTO $tableName (time,value) VALUES (?,?)"
        try {
            val preparedStmt = connection.prepareStatement(query)
            preparedStmt?.setTimestamp(1, timestamp)
            preparedStmt?.setBoolean(2, booleanValue)
            preparedStmt?.execute()
        } catch (e: SQLException) {
            println("Problem in inserting timestamp and booleanValue in the table: $tableName")
        }
    }

    /**
     * Allows to set timestamp and integerValue in the table.
     *
     * @param tableName  The <code>String</code> name of the table.
     * @param timestamp  The <code>Timestamp</code> to be added to the table.
     * @param integerValue  The <code>Int</code> to be added to the table.
     */
    override fun setIntegerValue(tableName: String, timestamp: Timestamp, integerValue: Int) {
        val query = "INSERT INTO $tableName (time,value) VALUES (?,?)"
        try {
            val preparedStmt = connection.prepareStatement(query)
            preparedStmt?.setTimestamp(1, timestamp)
            preparedStmt?.setInt(2, integerValue)
            preparedStmt?.execute()
        } catch (e: SQLException) {
            println("Problem in inserting timestamp and integerValue in the table: $tableName")
        }
    }

    /**
     * Counts the number of rows in the table and returns the value.
     *
     * @param tableName  The <code>String</code> name of the table.
     *
     * @return numberOfRows of <code>Int</code> type.
     */
    override fun getNumberOfRows(tableName: String): Int {
        val query = "SELECT COUNT(*) AS rows FROM $tableName"
        try {
            resultSet = statement.executeQuery(query)
        } catch (e: SQLException) {
            println("MySQL Making query problem")
        }

        var numberOfRows = 0
        while (resultSet!!.next()) {
            try {
                numberOfRows = resultSet!!.getInt("rows")
            } catch (e: SQLException) {
                println("MySQL getting Item TimeStamp/Value problem")
            }
        }
        return numberOfRows
    }

}