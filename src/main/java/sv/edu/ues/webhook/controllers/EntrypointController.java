package sv.edu.ues.webhook.controllers;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class EntrypointController {

    private static JacksonFactory factory = JacksonFactory.getDefaultInstance();

    @PostMapping
    public GoogleCloudDialogflowV2WebhookResponse main(@RequestBody String body) throws IOException {

        GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();

        GoogleCloudDialogflowV2WebhookRequest request =
                factory.createJsonParser(body).parse(GoogleCloudDialogflowV2WebhookRequest.class);

        System.out.println("[iNFO-INTENT]: "+request.getQueryResult().getIntent().getDisplayName());
        if(request.getQueryResult().getIntent().getDisplayName().equals("Eventos")){
            response.setFulfillmentText("Respuesta con spring webhook");
        }
        return response;
    }

}