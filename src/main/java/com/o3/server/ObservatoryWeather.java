package com.o3.server;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ObservatoryWeather {
    private String temperatureInKelvins;
    private String cloudinessPercentance;
    private String bagroundLightVolume;

    public String getBagroundLightVolume() {
        return bagroundLightVolume;
    }
    public void setBagroundLightVolume(String bagroundLightVolume) {
        this.bagroundLightVolume = bagroundLightVolume;
    }

    public String getTemperatureInKelvins() {
        return temperatureInKelvins;
    }
    public void setTemperatureInKelvins(String temperatureInKelvins) {
        this.temperatureInKelvins = temperatureInKelvins;
    }

    public void setCloudinessPercentance(String cloudinessPercentance) {
        this.cloudinessPercentance = cloudinessPercentance;
    }
    public String getCloudinessPercentance() {
        return cloudinessPercentance;
    }

    public void getWeather(double lat, double lon){
        try{
        URI uri = new URI("http://127.0.0.1:4001/wfs?latlon=" + lat + "," + lon);
        URL url = uri.toURL();
        InputStream inputStream = url.openStream();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        doc.getDocumentElement().normalize();
        Element root = doc.getDocumentElement();
        NodeList nodeList = doc.getElementsByTagName("wfs:member");
        
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node weatherNode = nodeList.item(i);
            if (weatherNode.getNodeType() == Node.ELEMENT_NODE) {
                Element weatherElement = (Element) weatherNode;
                Element bsWfsElement = (Element) weatherElement.getElementsByTagName("BsWfs:BsWfsElement").item(0);
                String parameterName = bsWfsElement.getElementsByTagName("BsWfs:ParameterName").item(0).getTextContent();
                String parameterValue = bsWfsElement.getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent();
                if (parameterName.equals("temperatureInKelvins")){
                    this.temperatureInKelvins = parameterValue;
                } else if(parameterName.equals("cloudinessPercentance")){
                    this.cloudinessPercentance = parameterValue;
                } else if(parameterName.equals("bagroundLightVolume")){
                    this.bagroundLightVolume = parameterValue;
                }
            }
        }
        inputStream.close(); 
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to get the weather!");
        }
    }

}
