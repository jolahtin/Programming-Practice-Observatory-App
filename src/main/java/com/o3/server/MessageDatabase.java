package com.o3.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;

public class MessageDatabase {

    private static MessageDatabase dbInstance = null;
    private Connection dbConnection = null;

    public static MessageDatabase getInstance(){
        if (dbInstance == null){
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    private MessageDatabase(){
        try {
            open("database");
        } catch (Exception e) {
            System.out.println("couldn't open");
        }
    }

    private void connect(String dbname) throws SQLException{
        String database = "jdbc:sqlite:" + dbname;
        dbConnection = DriverManager.getConnection(database);
    }

    private boolean newdb() throws SQLException{
        if (dbConnection != null){
            String createUserDB = "create table users (user varchar(25) NOT NULL PRIMARY KEY, password varchar(25) NOT NULL, email varchar(50) NOT NULL)";
            String createMessageDB = "create table messages (recordIdentifier varchar(100) NOT NULL, recordDescription varchar(500) NOT NULL, recordPayload varchar(500) NOT NULL, recordRightAscension varchar(20) NOT NULL, recordDeclination varchar(20) NOT NULL, recordTime varchar(25) NOT NULL)";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserDB);
            createStatement.executeUpdate(createMessageDB);
            createStatement.close();
            return true;
        }
        return false;
    }

    public void open(String dbname) throws SQLException{
        File db = new File(dbname);
        boolean exists = true;
        if (db.exists()){
            exists = true;
        } else {
            exists = false;
        }
        connect(dbname);
        if (!exists){
            newdb();
        }
    }

    public void closeDB() throws SQLException{
        if(dbConnection != null){
            dbConnection.close();
            dbConnection = null;
        }
    }

    public JSONArray getMessages() throws SQLException{
        JSONArray records = new JSONArray();
        ObservationRecord record = new ObservationRecord();
        Statement queryStatement = null;

        String statementString = "SELECT recordIdentifier, recordDescription, recordPayload, recordRightAscension, recordDeclination, recordTimeReceived FROM messages";
        queryStatement = dbConnection.createStatement();
        ResultSet results = queryStatement.executeQuery(statementString);

        while (results.next()){
            record.setRecordIdentifier(results.getString("recordIdentifier"));
            record.setRecordDescription(results.getString("recordDescription"));
            record.setRecordPayload(results.getString("recordPayload"));
            record.setRecordRightAscension(results.getString("recordRightAscension"));
            record.setRecordDeclination(results.getString("recordDeclination"));
            record.fetchRecordTime(results.getString("recordTimeReceived"));
            System.out.println(record.toString());
        }
        queryStatement.close();
        return records;
    }

    public void insertMessage(ObservationRecord record) throws SQLException {
        String insertString = "INSERT INTO messages (recordIdentifier, recordDescription, recordPayload, recordRightAscension, recordDeclination, recordTimeReceived) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStatement = dbConnection.prepareStatement(insertString)) {
            insertStatement.setString(1, record.getRecordIdentifier());
            insertStatement.setString(2, record.getRecordDescription());
            insertStatement.setString(3, record.getRecordPayload());
            insertStatement.setString(4, record.getRecordRightAscension());
            insertStatement.setString(5, record.getRecordDeclination());
            insertStatement.setString(6, record.getRecordTime());
            insertStatement.executeUpdate();
        }
    }

    public boolean login(String username, String password) throws SQLException{
        Statement queryStatement = null;

        String statementString = "SELECT user, password FROM users WHERE user='" + username + "'";
        queryStatement = dbConnection.createStatement();
        ResultSet results = queryStatement.executeQuery(statementString);
        if (results.getString("password").equals(password)){
            return true;
        }
        return false;
    }

    public void addUser(String username, String password, String email) throws SQLException{
        String insertString = "INSERT INTO users VALUES('" +
        username + "', '" +
        password + "', '" +
        email + "')";
        
        Statement insertStatement = dbConnection.createStatement();
        insertStatement.executeUpdate(insertString);
        insertStatement.close();
    }


}
