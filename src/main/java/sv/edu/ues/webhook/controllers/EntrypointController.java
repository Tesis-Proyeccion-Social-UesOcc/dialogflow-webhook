package sv.edu.ues.webhook.controllers;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.webhook.annotations.Invoker;

import java.io.IOException;

@RestController
public class EntrypointController {

    private static final JacksonFactory factory = JacksonFactory.getDefaultInstance();
    private final Invoker invoker;

    public EntrypointController(Invoker invoker) {
        this.invoker = invoker;
    }

    @PostMapping
    public GoogleCloudDialogflowV2WebhookResponse main(@RequestBody String body) throws IOException {

        GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();

        GoogleCloudDialogflowV2WebhookRequest request =
                factory.createJsonParser(body).parse(GoogleCloudDialogflowV2WebhookRequest.class);

        this.invoker.fulfill(
                request.getQueryResult().getIntent().getDisplayName(),
                response,
                request.getQueryResult().getParameters());

        return response;
    }

}