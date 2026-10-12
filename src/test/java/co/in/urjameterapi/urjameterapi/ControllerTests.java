package co.in.urjameterapi.urjameterapi;

import co.in.urjameterapi.urjameterapi.controller.*;
import co.in.urjameterapi.urjameterapi.exception.ApiExceptionHandler;
import co.in.urjameterapi.urjameterapi.mapper.PortalMapper;
import co.in.urjameterapi.urjameterapi.portal.UrjaPortalClient;
import co.in.urjameterapi.urjameterapi.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControllerTests {
    @Test void exportLiteralRouteDoesNotBecomeAMeterIdAndInvalidPageDoesNotCallPortal() throws Exception {
        var portal = mock(UrjaPortalClient.class);
        when(portal.exportMeters()).thenReturn(JsonMapper.builder().build().readTree("{\"data\":[],\"total\":0}"));
        var mvc = MockMvcBuilders.standaloneSetup(new MeterController(new MeterService(portal, new PortalMapper())))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/v1/meters/export")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/v1/meters").param("page", "0")).andExpect(status().isBadRequest());
        verify(portal, times(1)).exportMeters();
        verifyNoMoreInteractions(portal);
    }
    @Test void transformerPaginationIsForwarded() throws Exception {
        var portal = mock(UrjaPortalClient.class);
        when(portal.transformers(2)).thenReturn(JsonMapper.builder().build().readTree("""
                {"data":[],"total":40,"page":2,"pageSize":20}
                """));
        var mvc = MockMvcBuilders.standaloneSetup(new TransformerController(new TransformerService(portal, new PortalMapper()))).build();
        mvc.perform(get("/api/v1/transformers").param("page", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pagination.page").value(2));
        verify(portal).transformers(2);
    }
    @Test void detailsLocationAndReadingsHaveTheDocumentedShapes() throws Exception {
        var portal = mock(UrjaPortalClient.class);
        var json = JsonMapper.builder().build();
        when(portal.exportMeters()).thenReturn(json.readTree("""
                {"data":[{"meterId":"J100000","serialNo":"fixture","make":"HPL",
                  "phaseType":"single","installStatus":"Installed","dtCode":"DT-001",
                  "installType":"Whole Current","hierarchy":{"zone":{"code":"Z-01","name":"Zone"}},
                  "geo":{"lat":26.9,"lng":75.8}}],"total":1}
                """));
        when(portal.location("J100000")).thenReturn(json.readTree("""
                {"data":{"latitude":"26.9","longitude":"75.8"}}
                """));
        when(portal.readings("J100000")).thenReturn(json.readTree("""
                {"data":[{"timestamp":"24/06/2026 00:00","kwh":"123.45","kvah":"130","voltR":"229"}]}
                """));
        var mvc = MockMvcBuilders.standaloneSetup(new MeterController(new MeterService(portal, new PortalMapper())))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/v1/meters/J100000")).andExpect(status().isOk())
                .andExpect(jsonPath("$.meter.meterId").value("J100000"))
                .andExpect(jsonPath("$.hierarchy[0].code").value("Z-01"));
        mvc.perform(get("/api/v1/meters/J100000/location")).andExpect(status().isOk())
                .andExpect(jsonPath("$.location.latitude").value(26.9));
        mvc.perform(get("/api/v1/meters/J100000/readings")).andExpect(status().isOk())
                .andExpect(jsonPath("$.readings[0].kwh").value(123.45));
    }
}
