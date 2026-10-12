package co.in.urjameterapi.urjameterapi.service;

import co.in.urjameterapi.urjameterapi.dto.PageResponse;
import co.in.urjameterapi.urjameterapi.dto.Transformer;
import co.in.urjameterapi.urjameterapi.mapper.PortalMapper;
import co.in.urjameterapi.urjameterapi.portal.UrjaPortalClient;
import org.springframework.stereotype.Service;

@Service
public class TransformerService {
    private final UrjaPortalClient client; private final PortalMapper mapper;
    public TransformerService(UrjaPortalClient client, PortalMapper mapper) { this.client = client; this.mapper = mapper; }
    public PageResponse<Transformer> list(int page) {
        MeterService.validatePage(page); return mapper.page(client.transformers(page), page, mapper::transformer);
    }
}
