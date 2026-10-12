package co.in.urjameterapi.urjameterapi.dto;


public class LocationResponse {

    private String meterId;
    private Location location;

    public LocationResponse() {
    }

    public LocationResponse(String meterId, Location location) {
        this.meterId = meterId;
        this.location = location;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
