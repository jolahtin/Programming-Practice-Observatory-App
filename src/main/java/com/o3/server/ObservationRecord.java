package com.o3.server;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.time.Instant;

public class ObservationRecord {

    private String recordIdentifier;
    private String recordDescription;
    private String recordPayload;
    private String recordRightAscension;
    private String recordDeclination;
    private String recordTime;

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
        return recordTime;
    }
    public void setRecordTime(){
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMdd'T'HH:mm:ss.SSSX");
        this.recordTime = now.format(formatter); 
    }
    public long dateAsLong(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMdd'T'HH:mm:ss.SSSX");
        Date date = (Date) formatter.parse(recordTime);
        return date.getTime();
    }
    public void setLongTime(long epoch){
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMdd'T'HH:mm:ss.SSSX");
        this.recordTime = date.format(formatter); 
    }

}
