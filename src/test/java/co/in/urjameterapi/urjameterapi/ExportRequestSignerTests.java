package co.in.urjameterapi.urjameterapi;

import co.in.urjameterapi.urjameterapi.portal.ExportRequestSigner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExportRequestSignerTests {
    @Test void matchesIndependentHmacVector() {
        assertEquals("abd964eb50520417b352bf2543082efe28d0261100989f95e3b4238aec054233", new ExportRequestSigner().sign("test-only-secret", "1700000000"));
    }
}
