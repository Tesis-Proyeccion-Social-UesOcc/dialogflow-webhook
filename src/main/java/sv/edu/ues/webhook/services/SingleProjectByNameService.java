package sv.edu.ues.webhook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sv.edu.ues.webhook.utils.PayloadBuilder;

import java.util.Map;

public class SingleProjectByNameService implements ExternalResourcesHandler{

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Value("${external_resource.url_base}")
    private String baseUrl;
    private final RestTemplate client;
    private String carnet;
    private String projectName;

    public SingleProjectByNameService(RestTemplate template) {
        this.client = template;
    }

    @Override
    public String getExternalResourceUrl() {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("estudiantes/")
                .path(carnet)
                .path("/proyectos")
                .queryParam("projectName", projectName)
                .toUriString();
    }

    @Override
    public void externalCall(GoogleCloudDialogflowV2WebhookResponse response) {
        var uri = this.getExternalResourceUrl();

        JsonNode clientResponse;
        try {
            clientResponse = this.client.getForObject(uri, JsonNode.class);
        }catch (HttpClientErrorException e){
            logger.error(e.getMessage());
            response.setFulfillmentText("Algo anda mal, por favor intente en unos minutos");
            return;
        }
        assert clientResponse!= null;

        var message = clientResponse.get("message");
        if(message == null) {
            var builder = new StringBuilder();
            PayloadBuilder.buildForProjectInfo(clientResponse, builder);
            response.setFulfillmentText(builder.toString());
        }
        else
            response.setFulfillmentText(message.asText());
    }

    @Override
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params) {
        logger.info("Resolving request for SingleProjectByNameService, current params: {}", params);
        carnet = (String) params.get("carnet");
        projectName = (String) params.get("proyecto");
        if(!carnet.isBlank() && !projectName.isBlank())
            this.externalCall(response);
    }
}
