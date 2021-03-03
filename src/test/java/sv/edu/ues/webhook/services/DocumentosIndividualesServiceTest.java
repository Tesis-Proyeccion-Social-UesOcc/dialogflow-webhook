package sv.edu.ues.webhook.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.api.services.dialogflow.v2beta1.model.GoogleCloudDialogflowV2WebhookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import sv.edu.ues.webhook.utils.General;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentosIndividualesServiceTest {

    @Mock
    RestTemplate template;

    @InjectMocks
    DocumentosIndividualesService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getExternalResourceUrl() {
        var base = "https://www.google.com/";
        ReflectionTestUtils.setField(service, "baseUrl", base);
        ReflectionTestUtils.setField(service, "docName", "ficha inscripcion");
        assertEquals(base + "documentos/nombre/ficha%20inscripcion", service.getExternalResourceUrl());
    }

    @Test
    void externalCall() throws JsonProcessingException {
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        ReflectionTestUtils.setField(service, "baseUrl", "anything");
        ReflectionTestUtils.setField(service, "docName", "anything");
        var uri = "www.someurl.com";
        var body = "{\"result\": [{\"uri\":\""+uri+"\"}]}";
        var node = new ObjectMapper().readTree(body);
        Mockito.when(template.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(node);
        service.externalCall(response);
        var messages = response.getFulfillmentMessages();
        var payload = messages.get(0).getPayload();
        var obj1 = (Map)((Map)payload.get("facebook")).get("attachment");
        var obj2 = (TextNode)((Map)obj1.get("payload")).get("url");
        assertEquals(1, messages.size());
        assertEquals("\""+uri+"\"", obj2.toString());

    }

    @Test
    void handle() {
        var params = Map.of("documento",(Object) "");
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        service.handle(response, params);
        var messages = response.getFulfillmentMessages();
        var message = messages.get(0);
        var replies = message.getQuickReplies();
        assertEquals(1, messages.size());
        assertEquals(replies.getTitle(), "Seleccione el documento que necesita");
        assertEquals(replies.getQuickReplies(), List.of(General.DOCUMENT_OPTIONS));
    }
}