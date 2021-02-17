package sv.edu.ues.webhook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2IntentMessage;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2IntentMessageQuickReplies;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sv.edu.ues.webhook.annotations.IntentHandler;

import java.util.List;
import java.util.Map;

@Service
public class ProjectInfoHandler implements ExternalResourcesHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${external_resource.url_base}")
    private String baseUrl;
    private final RestTemplate client;

    public ProjectInfoHandler(RestTemplate client) {
        this.client = client;
    }

    @IntentHandler(intent = "InformacionProyectos")
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params) {
        logger.info("Resolving request for InformacionProyectos, current params: {}", params);

        var estado = (String) params.get("estado");
        var carnet = (String) params.get("carnet");

        if (estado == null || estado.isBlank()) {
            var options = List.of("Pendiente", "En proceso", "Completado", "Rechazado");
            var replies = new GoogleCloudDialogflowV2IntentMessageQuickReplies();
            replies.setTitle("Seleccione el estado del proyecto del que dese informacion");
            replies.setQuickReplies(options);

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
        return UriComponentsBuilder.fromUriString(this.baseUrl)
                .path("/proyectos")
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("status", 2)
                .toUriString();
    }

    @Override
    public void externalCall(GoogleCloudDialogflowV2WebhookResponse response) {
        var uri = this.getExternalResourceUrl();
        var clientResponse = client.getForObject(uri, JsonNode.class);
        var output = "";
        assert clientResponse != null;
        var content = clientResponse.get("content");
        if(content.isEmpty()) {
            output = "No posee proyectos con el estado dado";
            response.setFulfillmentText(output);
        }
        else{
            var builder = new StringBuilder();
            builder.append("Proyectos con el estado proporcionado:\n");
            for(var node: content){
                builder.append(node.get("nombre")).append(":\n")
                        .append("Duracion: ").append(node.get("duracion")).append("\n")
                        .append(node.get("interno").asBoolean()? "Proyecto de tipo interno\n":"Proyecto de tipo externo\n")
                        .append("Tutor: ").append(node.get("personal")).append("\n");
                builder.append("Estudiantes en el proyecto: ");
                var flag = false;
                for(var estudiantes: node.get("estudiantes")){
                    if(flag) builder.append(", ");
                    builder.append(estudiantes.get("carnet"));
                    flag = true;
                }
            }
            response.setFulfillmentText(builder.toString());
        }
    }
}
