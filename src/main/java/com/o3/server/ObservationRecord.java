package com.o3.server;

public class ObservationRecord {

    private String recordIdentifier;
    private String recordDescription;
    private String recordPayload;
    private String recordRightAscension;
    private String recordDeclination;

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

}
