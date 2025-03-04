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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Server implements HttpHandler {

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
					System.out.println(input);
					checkUsername(t, input);
					storeRecord(t, input);
					respond(t, "OK", 200);
				} else {
					respond(t, "missing fields", 412);
				}
				} catch (Exception e){
					respond(t, "Not proper JSON", 413);
				}
			}

		} else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
			sendJSONRecords(t);
		} else if (t.getRequestMethod().equalsIgnoreCase("PUT")){
        	String text = new BufferedReader(new InputStreamReader(t.getRequestBody(),StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
			String query = t.getRequestURI().getQuery();
            if(!t.getRequestHeaders().get("Content-Type").get(0).equalsIgnoreCase("application/json")){
                respond(t, "Incorrect Content Type", 411);
			}else{
				try{
					String owner = MessageDatabase.getInstance().getRecordOwnerName(extractId(query));
					if (!owner.equals(t.getPrincipal().getUsername())){
						respond(t, "User does not have permission to modify message", 400);
					}else{
						try{
							JSONObject input = new JSONObject(text.toString());
							if (recordCheck(input)){
								if (updateCheck(input)){
									checkUsername(t, input);
									updateRecord(extractId(query), input);
									respond(t, "OK", 200);
								} else{
									respond(t, "malformed fields", 400);
								}
							} else {
								respond(t, "missing fields", 412);
						}
						} catch (Exception e){
							respond(t, "Not proper JSON", 414);
						}
					}
				} catch (Exception e){
					respond(t, "Failed to find message", 400);
				} 
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

	private void storeRecord(HttpExchange t, JSONObject input) throws Exception{
		ObservationRecord record = new ObservationRecord();
		record.setRecordIdentifier(input.getString("recordIdentifier"));
		record.setRecordDescription(input.getString("recordDescription"));
		record.setRecordPayload(input.getString("recordPayload"));
		record.setRecordRightAscension(input.getString("recordRightAscension"));
		record.setRecordDeclination(input.getString("recordDeclination"));
		record.setRecordOwner(input.getString("recordOwner"));
		record.setRecordTimeReceived();
		if (input.has("observatory")){
			JSONArray observatoryArray = new JSONArray(input.getJSONArray("observatory"));
			record.setObservatory(buildObservatory(observatoryArray.getJSONObject(0)));
			if (input.has("observatoryWeather")){
				ObservatoryWeather weather = new ObservatoryWeather();
				weather.getWeather(record.getObservatory().getLatitude(), record.getObservatory().getLongitude());
				record.setObservatoryWeather(weather);
			}
		}
		try {
			MessageDatabase.getInstance().insertMessage(record, t.getPrincipal().getUsername());
		} catch (SQLException e) {
			System.out.println("Couldn't insert message to database!");
		}
	}

	private boolean recordCheck(JSONObject record) throws Exception{
		String[] fields = {"recordIdentifier", "recordDescription", "recordPayload", "recordRightAscension", "recordDeclination"};
		for(int i=0; i<fields.length; i++){
			if(checkJSON(record, fields[i]) == false){
				return false;
			}
		}
		if(record.has("observatory")){
			JSONArray observatoryArray = new JSONArray(record.getJSONArray("observatory"));
			String [] observatoryFields = {"observatoryName", "latitude", "longitude"};
			for (int i=0; i<observatoryFields.length; i++){
				if(checkJSON(observatoryArray.getJSONObject(0), observatoryFields[i]) == false){
					return false;
				}
			}
		}
		return true;
    }

	private boolean checkJSON (JSONObject record, String field) throws Exception{
		if(record.has(field)){
			if (record.isNull(field) || record.get(field).toString().trim().isEmpty()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	private void sendJSONRecords(HttpExchange t) throws IOException{
		ArrayList<ObservationRecord> observationRecords = new ArrayList<ObservationRecord>();
		JSONArray jsonRecords = new JSONArray();
		try {
			observationRecords = MessageDatabase.getInstance().getMessages();
			for(int i=0;i<observationRecords.size();i++){
				JSONObject recordObject = new JSONObject(observationRecords.get(i));
				if (observationRecords.get(i).getObservatory() != null){
					recordObject.put("observatory", buildJSONObservatory(observationRecords.get(i)));
				}
				if (observationRecords.get(i).getObservatoryWeather() != null){
					recordObject.put("observatoryWeather", buildWeather(observationRecords.get(i)));
				}
				jsonRecords.put(recordObject);
			}
			String output = jsonRecords.toString();
			System.out.println(output);
			respond(t, output, 201);
		} catch (SQLException e) {
			respond(t, "couldn't get records", 500);
		}
	}

	private void checkUsername(HttpExchange t, JSONObject input){
		if(input.has("recordOwner")){
			return;
		} else{
			try {
				input.put("recordOwner", (MessageDatabase.getInstance().getNickname(t.getPrincipal().getUsername())));
			} catch (JSONException e) {
				System.out.println("error handling JSON in checkUsername");
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println("error handling SQL in checkUsername");
				e.printStackTrace();
			}
		}
	}

	private Observatory buildObservatory(JSONObject input) throws JSONException{
		Observatory observatory = new Observatory();
		observatory.setObservatoryName(input.getString("observatoryName"));
		observatory.setLatitude(input.getFloat("latitude"));
		observatory.setLongitude(input.getFloat("longitude"));
		return observatory;
	}
	
	private JSONArray buildJSONObservatory(ObservationRecord record){
		Observatory recordObservatory = record.getObservatory();
		JSONArray observatory = new JSONArray();
		JSONObject observatoryRecords = new JSONObject(recordObservatory);
		observatory.put(observatoryRecords);
		return observatory;
	}

	private JSONArray buildWeather(ObservationRecord record){
		ObservatoryWeather weather = record.getObservatoryWeather();
		JSONArray observatoryWeather = new JSONArray();
		JSONObject weatherRecords = new JSONObject(weather);
		observatoryWeather.put(weatherRecords);
		System.out.println(observatoryWeather.toString());
		return observatoryWeather;
	}

    private void respond(HttpExchange t, String response, int code) throws IOException{
        OutputStream output = t.getResponseBody();
		byte [] bytes = response.getBytes("UTF-8");
		t.sendResponseHeaders(code, bytes.length);
		output.write(response.getBytes());
		output.flush();
		output.close();
    }

	private int extractId(String query) throws Exception{
		String[] pair = query.split("=");
		if(!pair[0].equals("id") || pair.length > 2){
			throw new Exception();
		}
		int id = Integer.parseInt(pair[1]);
		return id;
	}

	private void updateRecord(int id, JSONObject input) throws Exception{
		ObservationRecord record = new ObservationRecord();
		record.setId(id);
		record.setRecordIdentifier(input.getString("recordIdentifier"));
		record.setRecordDescription(input.getString("recordDescription"));
		record.setRecordPayload(input.getString("recordPayload"));
		record.setRecordRightAscension(input.getString("recordRightAscension"));
		record.setRecordDeclination(input.getString("recordDeclination"));
		record.setRecordOwner(input.getString("recordOwner"));
		if (input.has("observatory")){
			JSONArray observatoryArray = new JSONArray(input.getJSONArray("observatory"));
			record.setObservatory(buildObservatory(observatoryArray.getJSONObject(0)));
		}
		if (!input.isNull("updateReason")){
			record.setUpdateReason(input.getString("updateReason"));
		} else{
			record.setUpdateReason("N/A");
		}
		record.setModified();
		MessageDatabase.getInstance().updateMessage(record);
	}

	private boolean updateCheck(JSONObject input){
		if(input.has("updateReason")){
			return true;
		}
		return true;
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

		server.setExecutor(Executors.newCachedThreadPool()); 
        server.start();

		} catch (Exception e){
			e.printStackTrace();
		}
    }
}