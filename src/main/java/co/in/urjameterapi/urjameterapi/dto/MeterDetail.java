package co.in.urjameterapi.urjameterapi.dto;

import java.util.List;

public class MeterDetail {

    private MeterSummary meter;
    private String installationType;
    private Location location;
    private List<HierarchyEntry> hierarchy;

    public MeterDetail() {
    }

    public MeterDetail(MeterSummary meter, String installationType, Location location, List<HierarchyEntry> hierarchy) {
        this.meter = meter;
        this.installationType = installationType;
        this.location = location;
        this.hierarchy = hierarchy;
    }

    public MeterSummary getMeter() {
        return meter;
    }

    public void setMeter(MeterSummary meter) {
        this.meter = meter;
    }

    public String getInstallationType() {
        return installationType;
    }

    public void setInstallationType(String installationType) {
        this.installationType = installationType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<HierarchyEntry> getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(List<HierarchyEntry> hierarchy) {
        this.hierarchy = hierarchy;
    }
}
