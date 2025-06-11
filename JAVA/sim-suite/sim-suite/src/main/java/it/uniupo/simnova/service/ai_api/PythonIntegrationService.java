package it.uniupo.simnova.service.ai_api;

import it.uniupo.simnova.service.ai_api.model.DataRequest;
import it.uniupo.simnova.service.ai_api.model.DataResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class PythonIntegrationService {

    public DataResponse processData(DataRequest dataRequest) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8000/v1/run";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("message", dataRequest.getData());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        String processedData = response.getBody();

        return new DataResponse(processedData);
    }

    public Object getData(String id){
        return new Object();
    }
}
