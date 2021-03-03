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
import sv.edu.ues.webhook.utils.QuickRepliesBuilder;

import java.util.List;
import java.util.Map;

@Service
public class EncargadosService implements ExternalResourcesHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Value("${external_resource.url_base}")
    private String baseUrl;
    private String area;
    private final RestTemplate client;

    public EncargadosService(RestTemplate client) {
        this.client = client;
    }

    @Override
    public String getExternalResourceUrl() {
        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("personal/encargado")
                .queryParam("area", area)
                .toUriString();
    }

    @Override
    public void externalCall(GoogleCloudDialogflowV2WebhookResponse response) {
        JsonNode clientResponse;
        try {
            clientResponse = client.getForObject(getExternalResourceUrl(), JsonNode.class);
        }catch (HttpClientErrorException e){
            logger.error(e.getMessage());
            response.setFulfillmentText("Algo anda mal, por favor intenta en unos minutos");
            return;
        }
        assert clientResponse != null;
        var message = clientResponse.get("message");
        if(message == null) {
            var areaName = area.equals("general") ? "general" : clientResponse.get("departamento").asText();
                var responseText = String
                        .format("Encargado(a) del área %s:\n\n%s %s\n\nHorario de atención:\n%s\n\nNormalmente puede encontrarlo(a) en %s, o puede contactarlo al correo %s",
                                areaName,
                                clientResponse.get("nombre").asText(),
                                clientResponse.get("apellido").asText(),
                                clientResponse.get("horario").asText(),
                                clientResponse.get("ubicacion").asText().toLowerCase(),
                                clientResponse.get("email").asText());
                response.setFulfillmentText(responseText);

        }
        else {

            response.setFulfillmentText(message.asText());

        }
    }

    @Override
    @IntentHandler(intent = "InformacionEncargados")
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params) {
        logger.info("Resolving request for InformacionEncargados, current params: {}", params);
        area = (String) params.get("area");
        if(area.isBlank()){
            var replies = QuickRepliesBuilder
                    .build("¿De que área es el encargado del que solicita información?", General.AREA_OPTIONS);
            var messages = new GoogleCloudDialogflowV2IntentMessage();
            messages.setQuickReplies(replies);

            messages.setPlatform(PLATFORM);
            response.setFulfillmentMessages(List.of(messages));
        }
        else
            externalCall(response);
    }
}
