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
public class DocumentosIndividualesService implements ExternalResourcesHandler{

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Value("${external_resource.url_base}")
    private String baseUrl;

    private String docName;

    private final RestTemplate client;

    public DocumentosIndividualesService(RestTemplate client) {
        this.client = client;
    }

    @Override
    public String getExternalResourceUrl() {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("plantillas/")
                .path("nombre/")
                .path(docName)
                .toUriString();
    }

    /* {
     "facebook": {
        "attachment": {
            "type": "file",
            "payload": {
            "url": "https://example.com/file.pdf"
                }
            }
        }
     }
     * */
    @Override
    public void externalCall(GoogleCloudDialogflowV2WebhookResponse response) {
        JsonNode clientResponse;
        try{
            clientResponse = client.getForObject(this.getExternalResourceUrl(), JsonNode.class);
        }catch (HttpClientErrorException e){
            logger.error(e.getMessage());
            response.setFulfillmentText("Algo anda mal, por favor intente en unos minutos");
            return;
        }
        assert clientResponse!= null;
        var result = clientResponse.get("result");
        Map<String, Object> payload = null;
        for(var doc: result){
            payload = PayloadBuilder.buildForAttachment(doc.get("uri"));
        }
        var messages = new GoogleCloudDialogflowV2IntentMessage();
        messages.setPayload(payload).setPlatform(PLATFORM);
        response.setFulfillmentMessages(List.of(messages));
    }

    @IntentHandler(intent = "DocumentosIndividuales")
    public void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params) {
        logger.info("Resolving request for DocumentosIndividuales, current params: {}", params);
        docName = (String) params.get("documento");
        if(docName.isBlank() || docName == null){

            var replies = QuickRepliesBuilder
                    .build("Seleccione el documento que necesita", General.DOCUMENT_OPTIONS);

            var messages = new GoogleCloudDialogflowV2IntentMessage();
            messages.setQuickReplies(replies);
            messages.setPlatform(PLATFORM);
            response.setFulfillmentMessages(List.of(messages));
        }
        else{
            this.externalCall(response);
        }
    }
}
