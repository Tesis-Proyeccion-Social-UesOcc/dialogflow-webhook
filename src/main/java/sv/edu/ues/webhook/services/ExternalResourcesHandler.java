package sv.edu.ues.webhook.services;

import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;

public interface ExternalResourcesHandler extends Handler{
    String getExternalResourceUrl();
    void externalCall(GoogleCloudDialogflowV2WebhookResponse response);
}
