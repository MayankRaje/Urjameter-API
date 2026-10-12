package co.in.urjameterapi.urjameterapi.controller;

import co.in.urjameterapi.urjameterapi.dto.LocationResponse;
import co.in.urjameterapi.urjameterapi.dto.MeterDetail;
import co.in.urjameterapi.urjameterapi.dto.MeterExport;
import co.in.urjameterapi.urjameterapi.dto.MeterSummary;
import co.in.urjameterapi.urjameterapi.dto.PageResponse;
import co.in.urjameterapi.urjameterapi.dto.ReadingsResponse;
import co.in.urjameterapi.urjameterapi.service.MeterService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/meters", produces = "application/json")
public class MeterController {
    private final MeterService service;
    public MeterController(MeterService service) { this.service = service; }
    @GetMapping
    public PageResponse<MeterSummary> list(@RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page) { return service.list(search, page); }
    @GetMapping("/export") public MeterExport export() { return service.export(); }
    @GetMapping("/{id}") public MeterDetail detail(@PathVariable String id) { return service.detail(id); }
    @GetMapping("/{id}/location") public LocationResponse location(@PathVariable String id) { return service.location(id); }
    @GetMapping("/{id}/readings") public ReadingsResponse readings(@PathVariable String id) { return service.readings(id); }
}
