package com.o3.server;
import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONArray;
import org.json.JSONObject;


public class Server implements HttpHandler {

	private ArrayList<ObservationRecord> records = new ArrayList<ObservationRecord>();

    private Server() {
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

		if (t.getRequestMethod().equalsIgnoreCase("POST")) {
        	String text = new BufferedReader(new InputStreamReader(t.getRequestBody(),StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            if(!t.getRequestHeaders().get("Content-Type").get(0).equalsIgnoreCase("application/json")){
                respond(t, "Incorrect Content Type", 411);
            } else{
				try{
				JSONObject input = new JSONObject(text.toString());
				if (recordCheck(input)){
					storeRecord(input);
					respond(t, "OK", 200);
				} else {
					respond(t, "missing fields", 412);
				}
				} catch (Exception e){
					respond(t, "Not proper JSON", 413);
				}
			}

		} else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
			if (records.isEmpty()){
				t.sendResponseHeaders(204, -1);
			} else {
				sendJSONRecords(t);
			}
			
		} else {
			respond(t, "Not Supported", 400);
		}
    }

	private static SSLContext serverSSLContext(String[] args) throws Exception {
		char[] passphrase = args[1].toCharArray();
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(args[0]), passphrase);
	
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, passphrase);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		SSLContext ssl = SSLContext.getInstance("TLS");
		ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		return ssl;
	}

	private void storeRecord(JSONObject input){
		ObservationRecord record = new ObservationRecord();
		record.setRecordIdentifier(input.getString("recordIdentifier"));
		record.setRecordDescription(input.getString("recordDescription"));
		record.setRecordPayload(input.getString("recordPayload"));
		record.setRecordRightAscension(input.getString("recordRightAscension"));
		record.setRecordDeclination(input.getString("recordDeclination"));
		records.add(record);
	}

	private boolean recordCheck(JSONObject record){ 
        if(record.has("recordIdentifier") && record.has("recordDescription") && record.has("recordPayload") && record.has("recordRightAscension") && record.has("recordDeclination")){
			if(record.isNull("recordIdentifier") || record.isNull("recordDescription") || record.isNull("recordPayload") || record.isNull("recordRightAscension") || record.isNull("recordDeclination")){
				return false;
			} else {
				return true;
			}
        }
		System.out.println(record.toString());
        return false;
    }

	private void sendJSONRecords(HttpExchange t) throws IOException{
		if(records.isEmpty()){
			return;
		}

		JSONArray jsonrecords = new JSONArray();
		
		for(int i = 0; i < records.size(); i++){
			JSONObject jsonrecord = new JSONObject(records.get(i));
			jsonrecords.put(jsonrecord);
		}
		
		String output = jsonrecords.toString();
		respond(t, output, 201);
	}

    private void respond(HttpExchange t, String response, int code) throws IOException{
        OutputStream output = t.getResponseBody();
		byte [] bytes = response.getBytes("UTF-8");
		t.sendResponseHeaders(code, bytes.length);
		output.write(response.getBytes());
		output.flush();
		output.close();
    }

    public static void main(String[] args) throws Exception {
		try {
		HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
        SSLContext sslContext = serverSSLContext(args);
		server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
			public void configure (HttpsParameters params) {
				InetSocketAddress remote = params.getClientAddress();
				SSLContext c = getSSLContext();
				SSLParameters sslparams = c.getDefaultSSLParameters();
				params.setSSLParameters(sslparams);
 			}
		});

		UserAuthenticator userAuth = new UserAuthenticator();
		HttpContext context = server.createContext("/datarecord", new Server());
		server.createContext("/registration", new RegistrationHandler(userAuth));
		context.setAuthenticator(userAuth);

		server.setExecutor(null); 
        server.start();

		} catch (Exception e){
			e.printStackTrace();
		}
    }
}