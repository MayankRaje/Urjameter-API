package co.in.urjameterapi.urjameterapi.portal;

import co.in.urjameterapi.urjameterapi.config.PortalProperties;
import co.in.urjameterapi.urjameterapi.exception.PortalException;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PortalHttpTransport {
    private final PortalProperties properties;
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    private final HttpClient client;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public PortalHttpTransport(PortalProperties properties) {
        this.properties = properties;
        client = HttpClient.newBuilder().cookieHandler(cookies)
                .connectTimeout(properties.getConnectTimeout()).followRedirects(HttpClient.Redirect.NEVER).build();
    }

    public synchronized void clearSession() { cookies.getCookieStore().removeAll(); }

    public JsonNode login(String email, String password) {
        clearSession();
        return send("/login", Map.of(), Map.of("email", email, "password", password), Map.of());
    }

    public JsonNode get(String path, Map<String, String> query) { return send(path, query, null, Map.of()); }

    public JsonNode get(String path, Map<String, String> query, Map<String, String> headers) {
        return send(path, query, null, headers);
    }

    private JsonNode send(String path, Map<String, String> query, Map<String, String> form,
            Map<String, String> headers) {
        String suffix = query.isEmpty() ? "" : "?" + encode(query);
        String base = properties.getBaseUrl().replaceAll("/+$", "");
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path + suffix))
                .timeout(properties.getRequestTimeout()).header("Accept", "application/json");
        headers.forEach(request::header);
        if (form == null) request.GET();
        else request.header("Content-Type", "application/x-www-form-urlencoded")
                .header("x-sveltekit-action", "true").header("Origin", base).header("Referer", base + "/login")
                .POST(HttpRequest.BodyPublishers.ofString(encode(form)));
        try {
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new PortalException(502, "Portal returned HTTP " + response.statusCode() + " for " + path + ". Check the captured request requirements.");
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!contentType.toLowerCase().contains("application/json"))
                throw new PortalException(502, "Portal returned a non-JSON response. Session behavior needs verification.");
            JsonNode json = mapper.readTree(response.body());
            if (json == null || !json.isObject()) throw new PortalException(502, "Portal returned an invalid response.");
            return json;
        } catch (HttpTimeoutException e) {
            throw new PortalException(504, "Portal request timed out.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PortalException(503, "Portal request was interrupted.");
        } catch (IOException e) {
            throw new PortalException(502, "Could not reach the portal.");
        } catch (PortalException e) { throw e; }
        catch (RuntimeException e) { throw new PortalException(502, "Portal returned an invalid JSON response."); }
    }

    private static String encode(Map<String, String> values) {
        return values.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
    }
}

