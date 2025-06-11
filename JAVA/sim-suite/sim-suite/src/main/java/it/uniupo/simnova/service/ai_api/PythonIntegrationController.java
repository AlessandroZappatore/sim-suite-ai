package it.uniupo.simnova.service.ai_api;

import it.uniupo.simnova.service.ai_api.model.DataRequest;
import it.uniupo.simnova.service.ai_api.model.DataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PythonIntegrationController {

    @Autowired
    private PythonIntegrationService pythonIntegrationService;

    @PostMapping("/data")
    public ResponseEntity<DataResponse> getPythonData(@RequestBody DataRequest data) {
        return ResponseEntity.ok(pythonIntegrationService.processData(data));
    }
}
