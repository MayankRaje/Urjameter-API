package co.in.urjameterapi.urjameterapi.dto;

import java.util.List;

public class ReadingsResponse {

    private String meterId;
    private String timestampTimezone;
    private List<Reading> readings;

    public ReadingsResponse() {
    }

    public ReadingsResponse(String meterId, String timestampTimezone, List<Reading> readings) {
        this.meterId = meterId;
        this.timestampTimezone = timestampTimezone;
        this.readings = readings;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getTimestampTimezone() {
        return timestampTimezone;
    }

    public void setTimestampTimezone(String timestampTimezone) {
        this.timestampTimezone = timestampTimezone;
    }

    public List<Reading> getReadings() {
        return readings;
    }

    public void setReadings(List<Reading> readings) {
        this.readings = readings;
    }
}
