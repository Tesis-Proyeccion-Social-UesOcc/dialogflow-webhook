package sv.edu.ues.webhook.services;

import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;

import java.util.Map;

public interface Handler {
    String PLATFORM = "FACEBOOK";
    void handle(GoogleCloudDialogflowV2WebhookResponse response, Map<String, Object> params);
}
