package co.in.urjameterapi.urjameterapi.exception;

public class PortalException extends RuntimeException {
    private final int status;
    public PortalException(int status, String message) { super(message); this.status = status; }
    public int status() { return status; }
}
