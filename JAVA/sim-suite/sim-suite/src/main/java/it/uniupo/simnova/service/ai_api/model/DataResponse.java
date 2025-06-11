package it.uniupo.simnova.service.ai_api.model;

public class DataResponse {
    private String processedData;

    public DataResponse() {
    }

    public DataResponse(String processedData) {
        this.processedData = processedData;
    }

    public String getProcessedData() {
        return processedData;
    }

    public void setProcessedData(String processedData) {
        this.processedData = processedData;
    }
}
