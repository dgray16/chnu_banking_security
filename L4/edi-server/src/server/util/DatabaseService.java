package server.util;


import java.sql.*;

/**
 * For H2 DB.
 *
 * Created by a1 on 15.11.16.
 */
public class DatabaseService {

    private static final String DATABASE_DRIVER = "org.h2.Driver";

    private static final String DATABASE_CONNECTION = "jdbc:h2:file:" +
            "/Users/a1/Documents/CHNU/Комп'ютерний захист фінансової інформації/Банки/Labs/Лаб4/edi-server/EDIMessages";

    private static final String DATABASE_USER = "postgres";

    private static final String DATABASE_PASSWORD = "postgres";

    private static Connection connection;

    public static Connection createConnection() {
        /* If database does not exists, H2 will create it automatically */
        Connection databaseConnection = null;

        try {
            Class.forName(DATABASE_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            databaseConnection = DriverManager.getConnection(DATABASE_CONNECTION, DATABASE_USER, DATABASE_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        connection = databaseConnection;

        checkFirstRow();

        return databaseConnection;
    }

    /**
     * Checks first row from DB, to be sure that there is at least one row (default),
     * if row will be absent, we should create it.
     */
    private static void checkFirstRow() {
        Integer firstID = getFirstID();

        if ( firstID == null ) {
            insertDefaultRow();
        }
    }

    private static void insertDefaultRow() {
        Statement statement;

        try {
            statement = connection.createStatement();

            statement.execute("INSERT INTO MESSAGES(number) VALUES('0000')");

            statement.close();
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if ( connection != null ) {
            return connection;
        } else {
            throw new RuntimeException();
        }
    }

    public static String getLastNumber() {
        Statement statement;
        String value = "";

        try {
            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM MESSAGES ORDER BY id DESC LIMIT 1");

            while (resultSet.next()) {
                value = resultSet.getString("number");
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return value;
    }

    public static void setNewLastNumber(Integer newLastNumber) {
        Statement statement;

        try {
            statement = connection.createStatement();

            statement.execute("INSERT INTO MESSAGES (NUMBER) VALUES ('" + newLastNumber + "')");

            statement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static Integer getFirstID() {
        Statement statement;
        Integer valueToReturn = null;

        try {
            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT id FROM MESSAGES LIMIT 1");

            while (resultSet.next()) {
                valueToReturn = resultSet.getInt("id");
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return valueToReturn;
    }

    /** I left it here just for SQL purpose, to see how table was created */
    static void createMessagesTable() {
        Statement statement;

        try {
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            statement.execute("CREATE TABLE Messages(id int auto_increment primary key, number varchar(4));");

            statement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
