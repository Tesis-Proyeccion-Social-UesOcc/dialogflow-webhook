package sv.edu.ues.webhook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sv.edu.ues.webhook.annotations.IntentHandler;

import java.util.Map;

@Service
public class ProcesosInformationService implements ExternalResourcesHandler{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${external_resource.url_base}")
    private String baseUrl;
    private final RestTemplate client;

    public ProcesosInformationService(RestTemplate client) {
        this.client = client;
    }

    @Override
    public String getExternalResourceUrl() {
        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("procesos")
                .toUriString();
    }

    @Override
    public void externalCall(GoogleCloudDialogflowV2WebhookResponse response) {
        JsonNode clientResponse;
        try {
            clientResponse = client.getForObject(getExternalResourceUrl(), JsonNode.class);
        }
        catch (HttpClientErrorException e){
            logger.error(e.getMessage());
            response.setFulfillmentText("Algo anda mal, por favor intente en unos minutos");
            return;
        }
        assert clientResponse != null;
        var content = clientResponse.get("result");
        if(content.isEmpty()){
            response.setFulfillmentText("No se encontro información sobre los procesos");
        }
        else{
            var builder = new StringBuilder("Calendarización de procesos:\n\n");
            for(var proceso: content){
                builder.append(proceso.get("nombre").asText().toUpperCase())
                        .append("\n")
                        .append(proceso.get("descripcion").asText())
                        .append("\n")
                        .append("Calendarizacion de entregas: ")
                        .append(proceso.get("fechaInicio").asText())
                        .append(" / ")
                        .append(proceso.get("fechaFin").asText())
                        .append("\n\n");
            }
            response.setFulfillmentText(builder.toString());
        }
    }

    @Override
    @IntentHandler(intent = "InfoProcesos")
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params) {
        this.externalCall(response);
    }
}
