package com.o3.server;
import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


public class Server implements HttpHandler {

	private StringBuilder textDump = new StringBuilder("");

    private Server() {
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

		if (t.getRequestMethod().equalsIgnoreCase("POST")) {
        	String text = new BufferedReader(new InputStreamReader(t.getRequestBody(),StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
			textDump.append(text);
			t.sendResponseHeaders(200, -1);

		} else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
			OutputStream output = t.getResponseBody();
			String response = "";
			if (textDump.toString().equals(response)){
				response = "No messages"; //message sent if textDump is empty
			} else {
				response = textDump.toString();
			}
			byte [] bytes = response.getBytes("UTF-8");
			t.sendResponseHeaders(200, bytes.length);			
			output.write(response.getBytes());
			output.flush();
			output.close();
			
		} else {
			OutputStream output = t.getResponseBody();
			String response = "Not supported"; //message sent if using unsupported command
			t.sendResponseHeaders(400, response.length());
			output.write(response.getBytes());
			output.close();
		}
    }

    public static void main(String[] args) throws Exception {
        //create the http server to port 8001 with default logger
        HttpServer server = HttpServer.create(new InetSocketAddress(8001),0);
        //create context that defines path for the resource, in this case a "help"
        server.createContext("/datarecord", new Server());
        // creates a default executor
        server.setExecutor(null); 
        server.start(); 
    }
}