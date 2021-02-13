package sv.edu.ues.webhook.services;

import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2IntentMessage;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2IntentMessageQuickReplies;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sv.edu.ues.webhook.annotations.IntentHandler;

import java.util.List;
import java.util.Map;

@Service
public class ProjectInfoHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @IntentHandler(intent = "InformacionProyectos")
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params){
        logger.info("Resolving request for InformacionProyectos, current params: {}", params);
        var estado = (String) params.get("estado");
        var carnet = (String) params.get("carnet");
        if( estado == null || estado.isBlank()) {
            logger.info("Entro a primer IF");
            var options = List.of("Pendiente", "En proceso", "Completado", "Rechazado");
            var replies = new GoogleCloudDialogflowV2IntentMessageQuickReplies();
            replies.setTitle("Seleccione el estado del proyecto del que dese informacion");
            replies.setQuickReplies(options);

            var messages = new GoogleCloudDialogflowV2IntentMessage();
            messages.setQuickReplies(replies);
            messages.setPlatform(PLATFORM);
            response.setFulfillmentMessages(List.of(messages));
        }
        else if(carnet == null || carnet.isBlank()){
            response.setFulfillmentText("Por favor ingrese su carnet");
        }
        else{
            response.setFulfillmentText("Todo bien, gracias");
        }
    }
}
