package co.in.urjameterapi.urjameterapi.controller;

import co.in.urjameterapi.urjameterapi.dto.PageResponse;
import co.in.urjameterapi.urjameterapi.dto.Transformer;
import co.in.urjameterapi.urjameterapi.service.TransformerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/transformers", produces = "application/json")
public class TransformerController {
    private final TransformerService service;
    public TransformerController(TransformerService service) { this.service = service; }
    @GetMapping public PageResponse<Transformer> list(@RequestParam(defaultValue = "1") int page) { return service.list(page); }
}
