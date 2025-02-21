package com.o3.server;

import java.sql.SQLException;

public class UserAuthenticator extends com.sun.net.httpserver.BasicAuthenticator {

    public UserAuthenticator(){
        super("datarecord");
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        try {
            if(MessageDatabase.getInstance().login(username, password)){
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Login failed");
        }
        return false;
    }

    public boolean addUser(String username, String password, String email, String nick){
        try {
            MessageDatabase.getInstance().addUser(username, password, email, nick);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

}
