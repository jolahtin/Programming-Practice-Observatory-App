package com.o3.server;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ObservationRecord {

    private String recordIdentifier;
    private String recordDescription;
    private String recordPayload;
    private String recordRightAscension;
    private String recordDeclination;
    private String recordTimeReceived;

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

    public String getRecordTime(){
        return recordTimeReceived;
    }
    public void setRecordTime(){
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMdd'T'HH:mm:ss.SSSX");
        this.recordTimeReceived = now.format(formatter); 
    }
    public void fetchRecordTime(String time){
        this.recordTimeReceived = time;
    }

}
