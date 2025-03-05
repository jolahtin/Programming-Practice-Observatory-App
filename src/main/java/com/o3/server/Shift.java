package com.o3.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class Shift {
    private int shift;
    private String payload;

    public Shift(String payload, int shift){
        this.shift = shift;
        this.payload = payload;
    }

    public String getShift(){
        String newload = "empty";
        try{
            URI uri = new URI("http://localhost:4002/decipher?shift=" + shift);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try(OutputStream output = conn.getOutputStream()){
                byte[] input = payload.getBytes("utf-8");
                output.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))){
                String responseLine = null;
                while ((responseLine = reader.readLine()) != null){
                    response.append(responseLine.trim());
                }
            }
            newload = response.toString();

        } catch (Exception e){
            System.out.println("Couldn't get shift!");
        }
        return newload;
    }
}
