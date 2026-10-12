package co.in.urjameterapi.urjameterapi.portal;

import co.in.urjameterapi.urjameterapi.config.PortalProperties;
import co.in.urjameterapi.urjameterapi.exception.PortalException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class PortalSessionManager {
    private final PortalProperties properties;
    private final PortalHttpTransport transport;
    private boolean authenticated;

    public PortalSessionManager(PortalProperties properties, PortalHttpTransport transport) {
        this.properties = properties; this.transport = transport;
    }

    public synchronized void ensureAuthenticated() {
        if (authenticated) return;
        if (properties.getEmail() == null || properties.getEmail().isBlank()
                || properties.getPassword() == null || properties.getPassword().isBlank())
            throw new PortalException(503, "Configure URJA_PORTAL_EMAIL and URJA_PORTAL_PASSWORD.");
        JsonNode result = transport.login(properties.getEmail(), properties.getPassword());
        if (!"redirect".equals(result.path("type").asText()) || result.path("status").asInt() != 303
                || !"/meters".equals(result.path("location").asText()))
            throw new PortalException(502, "Portal login did not return the captured success result.");
        JsonNode probe = transport.get("/portal/meters/search", java.util.Map.of("q", "", "page", "1"));
        if (!probe.path("data").isArray() || !probe.path("total").isIntegralNumber())
            throw new PortalException(502, "Portal session could not be verified.");
        authenticated = true;
    }

    public synchronized void invalidate() { authenticated = false; }
}
