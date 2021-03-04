package sv.edu.ues.webhook.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

class EncargadosServiceTest {

    @Mock
    RestTemplate client;

    @InjectMocks
    EncargadosService service;
    public static final String BASE = "www.google.com/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(service, "baseUrl", BASE);
        ReflectionTestUtils.setField(service, "area", "general");
    }

    @Test
    void getExternalResourceUrl() {

        assertEquals(BASE + "personal/encargado?area=general", service.getExternalResourceUrl());
    }

    @Test
    void externalCallWithNonexistentMessageNode() throws JsonProcessingException {
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        var body = "{\"departamento\": \"general\", \"nombre\": \"Chris\", \"apellido\": \"Nolan\"," +
                " \"horario\":\"4-10\", \"ubicacion\":\"ues\", \"email\": \"some@gmail.com\"}";
        var result = new ObjectMapper().readTree(body);
        Mockito.when(client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(result);
        service.externalCall(response);
        var text = response.getFulfillmentText();
        assertTrue(text.contains("general"));
        assertTrue(text.contains("Chris"));
        assertTrue(text.contains("Nolan"));
        assertTrue(text.contains("4-10"));
        assertTrue(text.contains("ues"));
        assertTrue(text.contains("some@gmail.com"));
    }

    @Test
    void externalCallWithExistentMessageNode() throws JsonProcessingException {
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        var body = "{\"message\": \"error\"}";
        var result = new ObjectMapper().readTree(body);
        Mockito.when(client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(result);
        service.externalCall(response);
        var text = response.getFulfillmentText();
        assertTrue(text.contains("error"));
    }

    @Test
    void handle() {
        var params = Map.of("area", (Object) "");
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        service.handle(response, params);
        var messages = response.getFulfillmentMessages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).getQuickReplies().getTitle().contains("¿De que área"));
        assertEquals(List.of(General.AREA_OPTIONS),  messages.get(0).getQuickReplies().getQuickReplies());

    }
}