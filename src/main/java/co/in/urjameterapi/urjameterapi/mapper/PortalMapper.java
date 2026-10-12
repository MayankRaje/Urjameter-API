package co.in.urjameterapi.urjameterapi.mapper;

import co.in.urjameterapi.urjameterapi.dto.HierarchyEntry;
import co.in.urjameterapi.urjameterapi.dto.Location;
import co.in.urjameterapi.urjameterapi.dto.MeterDetail;
import co.in.urjameterapi.urjameterapi.dto.MeterExport;
import co.in.urjameterapi.urjameterapi.dto.MeterSummary;
import co.in.urjameterapi.urjameterapi.dto.PageResponse;
import co.in.urjameterapi.urjameterapi.dto.Pagination;
import co.in.urjameterapi.urjameterapi.dto.Reading;
import co.in.urjameterapi.urjameterapi.dto.Transformer;
import co.in.urjameterapi.urjameterapi.exception.PortalException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class PortalMapper {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final List<String> LEVELS = List.of("zone", "circle", "division", "subdivision", "substation", "feeder", "dt");

    public MeterSummary summary(JsonNode n) {
        return new MeterSummary(text(n, "meterId"), text(n, "serialNo"), text(n, "make"),
                text(n, "phaseType"), text(n, "installStatus"), text(n, "dtCode"));
    }
    public MeterDetail detail(JsonNode n) {
        List<HierarchyEntry> hierarchy = new ArrayList<>();
        JsonNode h = n.path("hierarchy");
        if (!h.isObject()) throw invalid();
        for (String level : LEVELS) {
            JsonNode entry = h.path(level);
            if (!entry.isMissingNode() && !entry.isNull()) hierarchy.add(new HierarchyEntry(level, hierarchyText(entry, "code"), hierarchyText(entry, "name")));
        }
        return new MeterDetail(summary(n), text(n, "installType"), coordinates(n.path("geo"), "lat", "lng"), List.copyOf(hierarchy));
    }
    public Location location(JsonNode root) { return coordinates(root.path("data"), "latitude", "longitude"); }
    private Location coordinates(JsonNode n, String lat, String lng) {
        BigDecimal latitude = decimal(n, lat), longitude = decimal(n, lng);
        if (latitude != null && latitude.abs().compareTo(BigDecimal.valueOf(90)) > 0
                || longitude != null && longitude.abs().compareTo(BigDecimal.valueOf(180)) > 0) throw invalid();
        return new Location(latitude, longitude);
    }
    public List<Reading> readings(JsonNode root) {
        List<Reading> result = new ArrayList<>();
        for (JsonNode n : array(root)) {
            try { result.add(new Reading(LocalDateTime.parse(text(n, "timestamp"), TIME),
                    decimal(n, "kwh"), decimal(n, "kvah"), decimal(n, "voltR"))); }
            catch (PortalException e) { throw e; }
            catch (RuntimeException e) { throw invalid(); }
        }
        return List.copyOf(result);
    }
    public Transformer transformer(JsonNode n) {
        return new Transformer(text(n, "code"), text(n, "name"), text(n, "feederCode"), decimal(n, "capacityKva"));
    }
    public <T> PageResponse<T> page(JsonNode root, int expectedPage, Function<JsonNode,T> mapping) {
        JsonNode data = array(root);
        long total = integer(root, "total");
        long page = integer(root, "page"), size = integer(root, "pageSize");
        if (page != expectedPage || size < 1 || size > Integer.MAX_VALUE || data.size() > size || total < data.size()) throw invalid();
        List<T> rows = new ArrayList<>(); data.forEach(n -> rows.add(mapping.apply(n)));
        return new PageResponse<>(List.copyOf(rows), new Pagination((int) page, (int) size, total,
                total / size + (total % size == 0 ? 0 : 1)));
    }
    public MeterExport export(JsonNode root) {
        JsonNode data = array(root);
        if (integer(root, "total") != data.size()) throw new PortalException(502, "Portal export is incomplete.");
        HashSet<String> ids = new HashSet<>(); List<MeterDetail> rows = new ArrayList<>();
        for (JsonNode n : data) {
            MeterDetail meter = detail(n);
            if (!ids.add(meter.getMeter().getMeterId())) throw new PortalException(502, "Portal export contains duplicate meter IDs.");
            rows.add(meter);
        }
        return new MeterExport(List.copyOf(rows), rows.size());
    }
    private static JsonNode array(JsonNode root) {
        JsonNode data = root.path("data"); if (!data.isArray()) throw invalid(); return data;
    }
    private static long integer(JsonNode n, String field) {
        JsonNode v = n.path(field);
        if (!v.isIntegralNumber() || !v.canConvertToLong() || v.asLong() < 0) throw invalid(); return v.asLong();
    }
    private static String hierarchyText(JsonNode n, String field) {
        JsonNode value = n.path(field);
        if (value.isMissingNode() || value.isNull()) return null;
        if (!value.isString()) throw invalid();
        return value.asText();
    }
    private static String text(JsonNode n, String field) {
        JsonNode v = n.path(field); if (!v.isString() || v.asText().isBlank()) throw invalid(); return v.asText();
    }
    private static BigDecimal decimal(JsonNode n, String field) {
        if (!n.isObject()) throw invalid();
        JsonNode v = n.path(field);
        if (v.isMissingNode() || v.isNull()) return null;
        if (!v.isNumber() && !v.isString()) throw invalid();
        try { return new BigDecimal(v.asText()); } catch (NumberFormatException e) { throw invalid(); }
    }
    private static PortalException invalid() { return new PortalException(502, "Portal response has invalid or unsupported data."); }
}
