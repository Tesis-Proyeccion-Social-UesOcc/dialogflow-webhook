package sv.edu.ues.webhook.utils;

import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2IntentMessageQuickReplies;
import org.springframework.lang.NonNull;

import java.util.List;

public class QuickRepliesBuilder {

    public static GoogleCloudDialogflowV2IntentMessageQuickReplies build(@NonNull String title, @NonNull String... options){
        var replies = new GoogleCloudDialogflowV2IntentMessageQuickReplies();
        var optionsList = List.of(options);
        replies.setTitle(title);
        replies.setQuickReplies(optionsList);
        return replies;
    }
}
