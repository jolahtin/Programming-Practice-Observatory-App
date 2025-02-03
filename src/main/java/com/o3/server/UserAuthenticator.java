package com.o3.server;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserAuthenticator extends com.sun.net.httpserver.BasicAuthenticator {

    private JSONArray users = null;

    public UserAuthenticator(){
        super("datarecord");
        users = new JSONArray();
        JSONObject dummy = new JSONObject();
        dummy.put("name", "dummy");
        dummy.put("password", "passwd");
        dummy.put("email", "dummy@email.com");
        users.put(dummy);
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        for(int i=0; i<users.length(); i++){
            if(users.getJSONObject(i).getString("name").equals(username)){
                if(users.getJSONObject(i).getString("password").equals(password)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addUser(String username, String password, String email){
        for(int i=0; i<users.length(); i++){
            if(users.getJSONObject(i).getString("name").equals(username)){
                return false;
            }
        }
        JSONObject user = new JSONObject();
        user.put("name", username);
        user.put("password", password);
        user.put("email", email);
        users.put(user);
        return true;
    }

}
