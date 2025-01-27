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
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;


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

	private static SSLContext serverSSLContext() throws Exception {
	char[] passphrase = "Jonio1".toCharArray();
	KeyStore ks = KeyStore.getInstance("JKS");
	ks.load(new FileInputStream(args[0]), args[1]);

	KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	kmf.init(ks, passphrase);

	TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	tmf.init(ks);

	SSLContext ssl = SSLContext.getInstance("TLS");
	ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	return ssl;
	}

    public static void main(String[] args) throws Exception {
		try {
		HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
        SSLContext sslContext = serverSSLContext();
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