package com.o3.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;

public class MessageDatabase {

    private static MessageDatabase dbInstance = null;
    private Connection dbConnection = null;
    private SecureRandom secure = new SecureRandom();

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
            String createUserDB = "create table users (user varchar(25) PRIMARY KEY, password varchar(25) NOT NULL, email varchar(50) NOT NULL, nick varchar(50) NOT NULL)";
            String createMessageDB = "create table messages (recordIdentifier varchar(100) NOT NULL, recordDescription varchar(500) NOT NULL, recordPayload varchar(500) NOT NULL, recordRightAscension varchar(20) NOT NULL, recordDeclination varchar(20) NOT NULL, recordTimeReceived varchar(25) NOT NULL, recordOwner varchar(50) NOT NULL, observatory INTEGER(64), FOREIGN KEY(observatory) REFERENCES observatory(observatoryID))";
            String createObservatoryDB = "create table observatory (observatoryId INTEGER PRIMARY KEY, observatoryName varchar(100) NOT NULL, latitude num(20) NOT NULL, longitude num(20) NOT NULL)";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserDB);
            createStatement.executeUpdate(createMessageDB);
            createStatement.executeUpdate(createObservatoryDB);
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

    public ArrayList<ObservationRecord> getMessages() throws SQLException{
        ArrayList<ObservationRecord> records = new ArrayList<ObservationRecord>();
        ObservationRecord record = new ObservationRecord();
        Statement queryStatement = null;

        String statementString = "SELECT recordIdentifier, recordDescription, recordPayload, recordRightAscension, recordDeclination, recordTimeReceived, recordOwner, observatory FROM messages";
        queryStatement = dbConnection.createStatement();
        ResultSet results = queryStatement.executeQuery(statementString);

        while (results.next()){
            record.setRecordIdentifier(results.getString("recordIdentifier"));
            record.setRecordDescription(results.getString("recordDescription"));
            record.setRecordPayload(results.getString("recordPayload"));
            record.setRecordRightAscension(results.getString("recordRightAscension"));
            record.setRecordDeclination(results.getString("recordDeclination"));
            record.fetchRecordTimeReceived(results.getString("recordTimeReceived"));
            record.setRecordOwner(results.getString("recordOwner"));
            if (results.getString("observatory") != null){
                record.setObservatory(getObservatory(results.getString("observatory")));
            }
            records.add(record);
        }
        queryStatement.close();
        return records;
    }

    public void insertMessage(ObservationRecord record) throws SQLException {
        String insertString = "INSERT INTO messages (recordIdentifier, recordDescription, recordPayload, recordRightAscension, recordDeclination, recordTimeReceived, recordOwner, observatory) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement insertStatement = dbConnection.prepareStatement(insertString);
            insertStatement.setString(1, record.getRecordIdentifier());
            insertStatement.setString(2, record.getRecordDescription());
            insertStatement.setString(3, record.getRecordPayload());
            insertStatement.setString(4, record.getRecordRightAscension());
            insertStatement.setString(5, record.getRecordDeclination());
            insertStatement.setString(6, record.getRecordTimeReceived());
            insertStatement.setString(7, record.getRecordOwner());
            if (record.getObservatory() != null){
                String observatoryId = checkObservatory(record.getObservatory());
                if(observatoryId == null){
                    addObservatory(record.getObservatory());
                    observatoryId = checkObservatory(record.getObservatory());
                }
                insertStatement.setString(8, observatoryId);
            } else {insertStatement.setString(8, null);};
            insertStatement.executeUpdate();
            insertStatement.close();
        } catch(Exception e){
            System.out.println("Failed to insert message:");
        }
    }

    public String getNickname(String username) throws SQLException{
        Statement queryStatement = null;

        String statementString = "SELECT nick FROM users WHERE user='" + username + "'";
        queryStatement = dbConnection.createStatement();
        ResultSet result = queryStatement.executeQuery(statementString);
        String nickname = result.getString(1);
        return nickname;
    }

    public void addObservatory(Observatory observatory) throws SQLException{
        String insertString = "INSERT INTO observatory (observatoryName, longitude, latitude)" +
        "VALUES (?, ?, ?)";
        try{
            PreparedStatement insertStatement = dbConnection.prepareStatement(insertString);
            insertStatement.setString(1, observatory.getObservatoryName());
            insertStatement.setFloat(2, observatory.getLongitude());
            insertStatement.setFloat(3, observatory.getLatitude());
            insertStatement.executeUpdate();
            insertStatement.close();
        } 
        catch (Exception e){
            System.out.println("failed to save observatory to database");
        }
    }

    private String checkObservatory(Observatory observatory){
        Statement queryStatement = null;
        try{
        String statementString = "SELECT observatoryID FROM observatory WHERE observatoryName = '" + observatory.getObservatoryName() +"', " +
                                "longitude = " + observatory.getLongitude() +", " +
                                "latitude = " + observatory.getLatitude() +"";
        queryStatement = dbConnection.createStatement();
        ResultSet results = queryStatement.executeQuery(statementString);
        queryStatement.close();
        return results.getString(0);
        } catch(Exception e){
        System.out.println("No observatory found");
        }
        return null;
    }

    private Observatory getObservatory(String id) throws SQLException{
        Statement queryStatement = null;
        Observatory newObservatory = new Observatory();

        String statementString = "SELECT observatoryName, longitude, latitude FROM observatory WHERE observatoryID = '" + id +"'";
        queryStatement = dbConnection.createStatement();
        ResultSet results = queryStatement.executeQuery(statementString);
        newObservatory.setObservatoryName(results.getString("observatoryName"));
        newObservatory.setLongitude(results.getInt("longitude"));
        newObservatory.setLatitude(results.getInt("latitude"));
        queryStatement.close();
        return newObservatory;
    }

    public void addUser(String username, String password, String email, String nick) throws SQLException{
        password = saltPassword(password);
        String insertString = "INSERT INTO users (user, password, email, nick)" +
        "VALUES (?, ?, ?, ?)";
        PreparedStatement insertStatement = dbConnection.prepareStatement(insertString);
        insertStatement.setString(1, username);
        insertStatement.setString(2, password);
        insertStatement.setString(3, email);
        insertStatement.setString(4, nick);
        insertStatement.executeUpdate();
        insertStatement.close();
    }

    public boolean login(String username, String password) throws SQLException{
        Statement queryStatement = null;

        String statementString = "SELECT user, password FROM users WHERE user='" + username + "'";
        queryStatement = dbConnection.createStatement();
        ResultSet results = queryStatement.executeQuery(statementString);

        String dbpass = results.getString("password");
        password = Crypt.crypt(password, dbpass);
        if (dbpass.equals(password)){
            return true;
        }
        return false;
    }

    private String saltPassword(String password){
        byte bytes[] = new byte[13];
        secure.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;
        salt = salt.replace('+', 'a');
        
        String hashedPassword = Crypt.crypt(password, salt);
        return hashedPassword;
    }
}
