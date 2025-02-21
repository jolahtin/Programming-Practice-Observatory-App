package com.o3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {

    private UserAuthenticator myAuth;

    public RegistrationHandler(UserAuthenticator auth){
        myAuth = auth;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

		if (t.getRequestMethod().equalsIgnoreCase("POST")) {
        	String text = new BufferedReader(new InputStreamReader(t.getRequestBody(),StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            if(!t.getRequestHeaders().get("Content-Type").get(0).equalsIgnoreCase("application/json")){
                respond(t, "Missing Content-Type", 411);
            }
            JSONObject user = new JSONObject(text);
            if(jsonCheck(user)){
                if(myAuth.addUser(user.getString("username"), user.getString("password"), user.getString("email"), user.getString("userNickname"))){
                    respond(t, "OK", 200);
                } else{
                    respond(t, "Ineligible Username", 403);
                }
            } else{
                respond(t, "Invalid format", 410);
            }
        } else {
			respond(t, "Not Supported", 400);
		}
    }

    //For sending responses in a less messy way
    private void respond(HttpExchange t, String response, int code) throws IOException{
        OutputStream output = t.getResponseBody();
		t.sendResponseHeaders(code, response.length());
		output.write(response.getBytes());
        output.flush();
		output.close();
    }

    private boolean jsonCheck(JSONObject user){
        if(user.has("username") && user.has("password") && user.has("email") && user.has("userNickname")){
            return true;
        }
        return false;
    }

}
