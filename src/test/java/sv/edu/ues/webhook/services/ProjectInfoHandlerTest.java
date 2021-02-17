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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProjectInfoHandlerTest {

    @Mock
    private RestTemplate client;

    @InjectMocks
    private ProjectInfoHandler handler;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleWithEstadoNull() {
        var map = Map.of("estado", "", "carnet", (Object)"");
        var response = new GoogleCloudDialogflowV2WebhookResponse();

        handler.handle(response, map);
        var messages = response.getFulfillmentMessages();
        var message = messages.get(0);
        var replies = message.getQuickReplies();
        assertEquals(1, messages.size());
        assertEquals(replies.getTitle(), "Seleccione el estado del proyecto del que dese informacion");
        assertEquals(replies.getQuickReplies(), List.of("Pendiente", "En proceso", "Completado", "Rechazado"));
    }

    @Test
    void handleWithCarnetNull() {
        var map = Map.of("estado", "pendiente", "carnet", (Object)"");
        var response = new GoogleCloudDialogflowV2WebhookResponse();

        handler.handle(response, map);
        assertEquals("Por favor ingrese su carnet", response.getFulfillmentText());
    }

    @Test
    void handleWithAllArgs() throws JsonProcessingException {
        var map = Map.of("estado", "pendiente", "carnet", (Object)"zh15002");
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        var json = "{\"content\": [" +
                "{\"nombre\": \"Test\", \"duracion\": 250, \"interno\": true, \"personal\": \"Luis Salazar\", \"estudiantes\": " +
                "[{\"carnet\": \"zh15002\"}]}]}";

        var body = new ObjectMapper().readTree(json);
        Mockito.when(this.client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(body);
        ReflectionTestUtils.setField(handler, "baseUrl", "http://www.google.com");
        handler.handle(response, map);

        assertTrue(response.getFulfillmentText().contains("Proyectos con el estado proporcionado:"));
        assertTrue(response.getFulfillmentText().contains("Duracion"));
        assertTrue(response.getFulfillmentText().contains("Estudiantes en el proyecto:"));
        assertTrue(response.getFulfillmentText().contains("zh15002"));
        assertTrue(response.getFulfillmentText().contains("250"));
        assertTrue(response.getFulfillmentText().contains("Proyecto de tipo interno"));
        assertTrue(response.getFulfillmentText().contains("Proyectos con el estado proporcionado:"));
        assertTrue(response.getFulfillmentText().contains("Luis Salazar"));
    }

    @Test
    void getExternalResourceUrl() {
        var baseUrl = "http://www.google.com";
        ReflectionTestUtils.setField(handler, "baseUrl", baseUrl);
        var result = handler.getExternalResourceUrl();
        assertEquals(result, baseUrl + "/proyectos?page=0&size=5&status=2");
    }
}