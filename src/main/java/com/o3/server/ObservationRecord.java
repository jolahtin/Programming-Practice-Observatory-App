package com.o3.server;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ObservationRecord {

    private int id;
    private String recordIdentifier;
    private String recordDescription;
    private String recordPayload;
    private String recordRightAscension;
    private String recordDeclination;
    private String recordTimeReceived;
    private String recordOwner;
    private Observatory observatory = null;
    private String updatereason = "N/A";
    private String modified = null;

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public String getRecordIdentifier() {
        return recordIdentifier;
    }
    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    public String getRecordDescription() {
        return recordDescription;
    }
    public void setRecordDescription(String recordDescription) {
        this.recordDescription = recordDescription;
    }

    public String getRecordDeclination() {
        return recordDeclination;
    }
    public void setRecordDeclination(String recordDeclination) {
        this.recordDeclination = recordDeclination;
    }

    public String getRecordPayload() {
        return recordPayload;
    }
    public void setRecordPayload(String recordPayload) {
        this.recordPayload = recordPayload;
    }
    
    public String getRecordRightAscension() {
        return recordRightAscension;
    }
    public void setRecordRightAscension(String recordRightAscension) {
        this.recordRightAscension = recordRightAscension;
    }

    public String getRecordTimeReceived(){
        return recordTimeReceived;
    }
    public void setRecordTimeReceived(){
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        this.recordTimeReceived = now.format(formatter); 
    }
    public void fetchRecordTimeReceived(String time){
        this.recordTimeReceived = time;
    }

    public void setRecordOwner(String recordOwner) {
        this.recordOwner = recordOwner;
    }
    public String getRecordOwner() {
        return recordOwner;
    }

    public Observatory getObservatory() {
        return observatory;
    }
    public void setObservatory(Observatory observatory) {
        this.observatory = observatory;
    }

    public void setUpdatereason(String updatereason) {
        this.updatereason = updatereason;
    }
    public String getUpdatereason() {
        return updatereason;
    }

    public void setModified() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        this.recordTimeReceived = now.format(formatter); 
    }
    public void fetchModified(String modified){
        this.modified = modified;
    }
    public String getModified() {
        return modified;
    }

}
