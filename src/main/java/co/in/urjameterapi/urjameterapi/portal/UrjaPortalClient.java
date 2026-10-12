package co.in.urjameterapi.urjameterapi.portal;

import co.in.urjameterapi.urjameterapi.exception.PortalException;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class UrjaPortalClient {
    private final PortalSessionManager session;
    private final PortalHttpTransport transport;
    private final ExportRequestSigner signer;
    public UrjaPortalClient(PortalSessionManager session, PortalHttpTransport transport, ExportRequestSigner signer) {
        this.session = session; this.transport = transport; this.signer = signer;
    }
    public JsonNode searchMeters(String query, int page) {
        return read("/portal/meters/search", Map.of("q", query, "page", Integer.toString(page)));
    }
    public JsonNode location(String id) { return read("/portal/meters/" + id + "/geo", Map.of()); }
    public JsonNode readings(String id) { return read("/portal/meters/" + id + "/energy", Map.of()); }
    public JsonNode transformers(int page) { return read("/portal/dts", Map.of("page", Integer.toString(page))); }
    public JsonNode exportMeters() {
        JsonNode key = read("/portal/keys", Map.of()).path("data").path("signingSecret");
        if (!key.isString() || key.asText().isBlank())
            throw new PortalException(502, "Portal did not provide a valid export signing key.");
        String timestamp = Long.toString(java.time.Instant.now().getEpochSecond());
        return transport.get("/portal/export", Map.of("page", "1"), Map.of(
                "x-timestamp", timestamp, "x-signature", signer.sign(key.asText(), timestamp)));
    }
    private JsonNode read(String path, Map<String, String> query) {
        session.ensureAuthenticated();
        try { return transport.get(path, query); }
        catch (PortalException e) {
            // No automatic replay: captured expiry and signing failure signals are not yet established.
            if (e.status() == 502) session.invalidate();
            throw e;
        }
    }
}
