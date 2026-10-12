package co.in.urjameterapi.urjameterapi;

import co.in.urjameterapi.urjameterapi.mapper.PortalMapper;
import co.in.urjameterapi.urjameterapi.exception.PortalException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

class PortalMapperTests {
    private final JsonMapper json = JsonMapper.builder().build();
    private final PortalMapper mapper = new PortalMapper();
    @Test void preservesMissingReadingsAndParsesDecimalStrings() {
        var result = mapper.readings(json.readTree("""
                {"data":[{"timestamp":"24/06/2026 00:00","kwh":"48439.16","kvah":null,"voltR":"229"}]}
                """));
        assertEquals("48439.16", result.getFirst().getKwh().toPlainString());
        assertNull(result.getFirst().getKvah());
        assertEquals(2026, result.getFirst().getTimestamp().getYear());
    }
    @Test void rejectsInvalidDatesAndNumbers() {
        assertThrows(PortalException.class, () -> mapper.readings(json.readTree("""
                {"data":[{"timestamp":"31/02/2026 00:00","kwh":"1"}]}
                """)));
        assertThrows(PortalException.class, () -> mapper.location(json.readTree("""
                {"data":{"latitude":"NaN","longitude":"75"}}
                """)));
    }
    @Test void emptySearchIsAValidPage() {
        var result = mapper.page(json.readTree("""
                {"data":[],"total":0,"page":1,"pageSize":20}
                """), 1, mapper::summary);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getPagination().getTotalPages());
    }
    @Test void incompleteExportIsNotAdvertisedAsComplete() {
        assertThrows(PortalException.class, () -> mapper.export(json.readTree("{\"data\":[],\"total\":403}")));
    }
    @Test void paginationMismatchIsRejected() {
        assertThrows(PortalException.class, () -> mapper.page(json.readTree("""
                {"data":[],"total":0,"page":2,"pageSize":20}
                """), 1, mapper::summary));
    }
    @Test void blankHierarchyFieldsDoNotBlockValidMeters() {
        var result = mapper.detail(json.readTree("""
                {"meterId":"J100011","serialNo":"fixture","make":"HPL","phaseType":"three",
                 "installStatus":"Installed","dtCode":"DT-012","installType":"Whole Current",
                 "geo":{"lat":26.9,"lng":75.8},"hierarchy":{"circle":{"code":"","name":"Circle"}}}
                """));
        assertEquals("", result.getHierarchy().getFirst().getCode());
        assertEquals("Circle", result.getHierarchy().getFirst().getName());
    }
}
