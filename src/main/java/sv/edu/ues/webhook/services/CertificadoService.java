package sv.edu.ues.webhook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2IntentMessage;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sv.edu.ues.webhook.annotations.IntentHandler;
import sv.edu.ues.webhook.utils.PayloadBuilder;

import java.util.List;
import java.util.Map;

@Service
public class CertificadoService implements ExternalResourcesHandler {

    @Value("${external_resource.url_base}")
    private String baseUrl;
    private String carnet, projectName;
    private final RestTemplate client;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CertificadoService(RestTemplate client) {
        this.client = client;
    }

    @Override
    public String getExternalResourceUrl() {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("certificados/")
                .path(carnet)
                .queryParam("projectName", projectName)
                .toUriString();
    }

    @Override
    public void externalCall(GoogleCloudDialogflowV2WebhookResponse response) {
        JsonNode clientResponse;
        try {
            clientResponse = this.client.getForObject(this.getExternalResourceUrl(), JsonNode.class);
        }catch (HttpClientErrorException e){
            logger.error(e.getMessage());
            response.setFulfillmentText("Algo anda mal, por favor intente en unos minutos");
            return;
        }
        assert clientResponse!= null;

        var message = clientResponse.get("message");
        if(message == null) {
            var payload = PayloadBuilder.build(clientResponse.get("uri"));

            var messages = new GoogleCloudDialogflowV2IntentMessage();
            messages.setPayload(payload).setPlatform(PLATFORM);
            response.setFulfillmentMessages(List.of(messages));
        }
        else
            response.setFulfillmentText(message.asText());
    }

    @Override
    @IntentHandler(intent = "Certificado")
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params) {
        logger.info("Resolving request for Certificado, current params: {}", params);
        carnet = (String) params.get("carnet");
        projectName = (String) params.get("proyecto");
        if(!carnet.isBlank() && !projectName.isBlank())
            this.externalCall(response);
    }
}
