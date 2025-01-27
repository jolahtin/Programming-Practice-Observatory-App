package com.o3.server;

import java.util.Hashtable;
import java.util.Map;

public class UserAuthenticator extends com.sun.net.httpserver.BasicAuthenticator {

    private Map<String, String> users = null;

    public UserAuthenticator(){
        super("datarecord");
        users = new Hashtable<String,String>();
        users.put("dummy", "passwd");
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        if(users.get(username).equals(password)){
            return true;
        }
        return false;
    }

    public boolean addUser(String username, String password){
        if(users.containsKey(username)){
            return false;
        }
        users.put(username, password);
        return true;
    }

}
