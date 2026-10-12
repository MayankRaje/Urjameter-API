package co.in.urjameterapi.urjameterapi;

import co.in.urjameterapi.urjameterapi.config.PortalProperties;
import co.in.urjameterapi.urjameterapi.exception.PortalException;
import co.in.urjameterapi.urjameterapi.portal.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PortalIntegrationTests {
    @Test void loginCookieIsRetainedAndSessionIsReused() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger logins = new AtomicInteger();
        server.createContext("/login", e -> {
            logins.incrementAndGet();
            String form = new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(form.contains("password=a%26b%3Dc"));
            assertEquals("true", e.getRequestHeaders().getFirst("x-sveltekit-action"));
            e.getResponseHeaders().add("Set-Cookie", "test_session=fixture; Path=/; HttpOnly");
            byte[] body = "{\"type\":\"redirect\",\"status\":303,\"location\":\"/meters\"}".getBytes(StandardCharsets.UTF_8);
            e.getResponseHeaders().add("Content-Type", "application/json");
            e.sendResponseHeaders(200, body.length); e.getResponseBody().write(body); e.close();
        });
        server.createContext("/portal/meters/search", e -> {
            assertTrue(e.getRequestHeaders().getFirst("Cookie").contains("test_session=fixture"));
            byte[] body = "{\"data\":[],\"total\":0,\"page\":1,\"pageSize\":20}".getBytes(StandardCharsets.UTF_8);
            e.getResponseHeaders().add("Content-Type", "application/json");
            e.sendResponseHeaders(200, body.length); e.getResponseBody().write(body); e.close();
        });
        server.start();
        try {
            var properties = new PortalProperties("http://127.0.0.1:" + server.getAddress().getPort(), "fixture@example.invalid", "a&b=c", Duration.ofSeconds(2), Duration.ofSeconds(3));
            var transport = new PortalHttpTransport(properties);
            var session = new PortalSessionManager(properties, transport);
            var client = new UrjaPortalClient(session, transport, new ExportRequestSigner());
            assertTrue(client.searchMeters("", 1).path("data").isArray());
            client.searchMeters("", 1);
            assertEquals(1, logins.get());
        } finally { server.stop(0); }
    }
    @Test void redirectsAreNotFollowedOrMistakenForEmptyData() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/portal/test", e -> { e.getResponseHeaders().add("Location", "/login"); e.sendResponseHeaders(303, -1); e.close(); });
        server.start();
        try {
            var properties = new PortalProperties("http://127.0.0.1:" + server.getAddress().getPort(), "", "", Duration.ofSeconds(2), Duration.ofSeconds(3));
            var error = assertThrows(PortalException.class, () -> new PortalHttpTransport(properties).get("/portal/test", Map.of()));
            assertEquals(502, error.status());
        } finally { server.stop(0); }
    }
}
