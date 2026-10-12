package co.in.urjameterapi.urjameterapi.dto;


public class MeterSummary {

    private String meterId;
    private String serialNumber;
    private String make;
    private String phaseType;
    private String installationStatus;
    private String transformerCode;

    public MeterSummary() {
    }

    public MeterSummary(String meterId, String serialNumber, String make, String phaseType, String installationStatus, String transformerCode) {
        this.meterId = meterId;
        this.serialNumber = serialNumber;
        this.make = make;
        this.phaseType = phaseType;
        this.installationStatus = installationStatus;
        this.transformerCode = transformerCode;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getPhaseType() {
        return phaseType;
    }

    public void setPhaseType(String phaseType) {
        this.phaseType = phaseType;
    }

    public String getInstallationStatus() {
        return installationStatus;
    }

    public void setInstallationStatus(String installationStatus) {
        this.installationStatus = installationStatus;
    }

    public String getTransformerCode() {
        return transformerCode;
    }

    public void setTransformerCode(String transformerCode) {
        this.transformerCode = transformerCode;
    }
}
