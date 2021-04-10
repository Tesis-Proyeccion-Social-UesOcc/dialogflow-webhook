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
import sv.edu.ues.webhook.utils.General;
import sv.edu.ues.webhook.utils.PayloadBuilder;
import sv.edu.ues.webhook.utils.QuickRepliesBuilder;

import java.util.List;
import java.util.Map;

@Service
public class ProjectInfoService implements ExternalResourcesHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${external_resource.url_base}")
    private String baseUrl;
    private final RestTemplate client;
    private String status;
    private String carnet;

    public ProjectInfoService(RestTemplate client) {
        this.client = client;
    }

    @IntentHandler(intent = "InformacionProyectos")
    @Override
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params) {
        logger.info("Resolving request for InformacionProyectos, current params: {}", params);

        status = (String) params.get("estado");
        carnet = (String) params.get("carnet");

        if (status == null || status.isBlank()) {

            var replies = QuickRepliesBuilder
                    .build("Seleccione el estado del proyecto del que desea información", General.STATUS_OPTIONS);

            var messages = new GoogleCloudDialogflowV2IntentMessage();
            messages.setQuickReplies(replies);
            messages.setPlatform(PLATFORM);
            response.setFulfillmentMessages(List.of(messages));
        } else if (carnet == null || carnet.isBlank()) {
            response.setFulfillmentText("Por favor ingrese su carnet");
        } else {
            this.externalCall(response);
        }
    }

    @Override
    public String getExternalResourceUrl() {
        var statusId = General.STATUSES.get(status);
        return UriComponentsBuilder.fromUriString(this.baseUrl)
                .path("estudiantes/")
                .path(carnet)
                .path("/proyectos")
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("status", statusId)
                .toUriString();
    }

    @Override
    public void externalCall(GoogleCloudDialogflowV2WebhookResponse response) {
        var uri = this.getExternalResourceUrl();
        JsonNode clientResponse;
        try {
            clientResponse = client.getForObject(uri, JsonNode.class);
        }catch (HttpClientErrorException e){
            logger.error(e.getMessage());
            response.setFulfillmentText("Algo anda mal, por favor intente en unos minutos");
            return;
        }

        var output = "";
        assert clientResponse != null;
        var content = clientResponse.get("content");
        if(content.isEmpty()) {
            output = "No posee proyectos con el estado proporcionado";
            response.setFulfillmentText(output);
        }
        else{
            var builder = new StringBuilder();
            for(var node: content){
                PayloadBuilder.buildForProjectInfo(node, builder);
            }
            response.setFulfillmentText(builder.toString());
        }
    }
}
