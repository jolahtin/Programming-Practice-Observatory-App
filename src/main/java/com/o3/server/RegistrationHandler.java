package com.o3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
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
            if(text.contains(":")){
                String[] usepass = text.split(":");
                if(myAuth.addUser(usepass[0], usepass[1])){
                    respond(t, "OK", 200);
                } else{
                    respond(t, "Ineligible Username", 403);
                }
            } else {
                respond(t, "Invalid Username and Password format", 400);
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

}
