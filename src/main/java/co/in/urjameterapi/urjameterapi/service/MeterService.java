package co.in.urjameterapi.urjameterapi.service;

import co.in.urjameterapi.urjameterapi.dto.LocationResponse;
import co.in.urjameterapi.urjameterapi.dto.MeterDetail;
import co.in.urjameterapi.urjameterapi.dto.MeterExport;
import co.in.urjameterapi.urjameterapi.dto.MeterSummary;
import co.in.urjameterapi.urjameterapi.dto.PageResponse;
import co.in.urjameterapi.urjameterapi.dto.ReadingsResponse;
import co.in.urjameterapi.urjameterapi.exception.PortalException;
import co.in.urjameterapi.urjameterapi.mapper.PortalMapper;
import co.in.urjameterapi.urjameterapi.portal.UrjaPortalClient;
import org.springframework.stereotype.Service;

@Service
public class MeterService {
    private final UrjaPortalClient client;
    private final PortalMapper mapper;
    public MeterService(UrjaPortalClient client, PortalMapper mapper) { this.client = client; this.mapper = mapper; }
    public PageResponse<MeterSummary> list(String search, int page) {
        validatePage(page);
        if (search == null || search.length() > 200) throw new IllegalArgumentException();
        return mapper.page(client.searchMeters(search, page), page, mapper::summary);
    }
    public MeterDetail detail(String id) {
        validateId(id);
        // Live complete export avoids a stale snapshot miss. Absence semantics still require verification.
        return export().getData().stream().filter(m -> m.getMeter().getMeterId().equals(id)).findFirst()
                .orElseThrow(() -> new PortalException(502, "Meter was absent from the export; portal not-found behavior is not verified."));
    }
    public LocationResponse location(String id) { validateId(id); return new LocationResponse(id, mapper.location(client.location(id))); }
    public ReadingsResponse readings(String id) { validateId(id); return new ReadingsResponse(id, null, mapper.readings(client.readings(id))); }
    public MeterExport export() { return mapper.export(client.exportMeters()); }
    public static void validatePage(int page) { if (page < 1) throw new IllegalArgumentException(); }
    public static void validateId(String id) {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,100}")) throw new IllegalArgumentException();
    }
}
