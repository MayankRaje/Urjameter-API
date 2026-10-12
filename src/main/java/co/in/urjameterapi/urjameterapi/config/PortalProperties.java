package co.in.urjameterapi.urjameterapi.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "urja.portal")
public class PortalProperties {

    private String baseUrl;
    private String email;
    private String password;
    private Duration connectTimeout;
    private Duration requestTimeout;

    public PortalProperties() {
    }

    public PortalProperties(String baseUrl, String email, String password, Duration connectTimeout, Duration requestTimeout) {
        this.baseUrl = baseUrl;
        this.email = email;
        this.password = password;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
}
