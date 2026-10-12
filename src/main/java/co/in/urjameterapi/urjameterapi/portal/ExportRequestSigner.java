package co.in.urjameterapi.urjameterapi.portal;

import co.in.urjameterapi.urjameterapi.exception.PortalException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class ExportRequestSigner {
    public String sign(String secret, String timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String message = String.join("\n", "GET", "/portal/export", "page=1", timestamp);
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new PortalException(503, "Export signature could not be generated.");
        }
    }
}
