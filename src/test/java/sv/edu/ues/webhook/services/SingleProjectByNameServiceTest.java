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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SingleProjectByNameServiceTest {

    @Mock
    private RestTemplate client;

    @InjectMocks
    private SingleProjectByNameService handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getExternalResourceUrl() {
        String base = "www.google.com/", carnet="zh15002", project="test";
        setFields(base, carnet, project);
        var result = handler.getExternalResourceUrl();

        assertEquals(base + "estudiantes/" + carnet + "/proyectos/single?projectName="+project, result);

    }

    @Test
    void externalCallWithMessage() throws JsonProcessingException {
        setFields("base", "carnet", "project"); // dummy values
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        var message = "message";
        var json = "{\"message\": \""+message+"\"}";
        var toReturn = new ObjectMapper().readTree(json);
        Mockito.when(this.client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(toReturn);

        handler.externalCall(response);

        assertEquals(message, response.getFulfillmentText());
    }

    @Test
    void externalCallWithNoMessage() throws JsonProcessingException {
        setFields("base", "carnet", "project"); // dummy values
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        var json = "{\"nombre\": \"Test\", \"duracion\": 250, \"interno\": true, \"personal\": \"Luis Salazar\"," +
                " \"estudiantes\": [{\"carnet\": \"zh15002\"}]}";

        var body = new ObjectMapper().readTree(json);
        Mockito.when(this.client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(body);

        ReflectionTestUtils.setField(handler, "baseUrl", "http://www.google.com");
        handler.externalCall(response);

        assertTrue(response.getFulfillmentText().contains("Duracion"));
        assertTrue(response.getFulfillmentText().contains("Estudiantes en el proyecto:"));
        assertTrue(response.getFulfillmentText().contains("zh15002"));
        assertTrue(response.getFulfillmentText().contains("250"));
        assertTrue(response.getFulfillmentText().contains("Proyecto de tipo interno"));
        assertTrue(response.getFulfillmentText().contains("Luis Salazar"));
        assertFalse(response.getFulfillmentText().contains("Estado de los documentos requeridos"));
    }

    @Test
    void externalCallWithNoMessageAndDocuments() throws JsonProcessingException {
        setFields("base", "carnet", "project"); // dummy values
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        var json = "{\"nombre\": \"Test\", \"duracion\": 250, \"interno\": true, \"personal\": \"Luis Salazar\", \"estudiantes\": " +
                "[{\"carnet\": \"zh15002\"}], \"documentos\": [{\"nombre\": \"doc1\", \"entregado\": true, \"aprobado\": false}]}";

        var body = new ObjectMapper().readTree(json);
        Mockito.when(this.client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(body);

        ReflectionTestUtils.setField(handler, "baseUrl", "http://www.google.com");
        handler.externalCall(response);

        assertTrue(response.getFulfillmentText().contains("Duracion"));
        assertTrue(response.getFulfillmentText().contains("Estudiantes en el proyecto:"));
        assertTrue(response.getFulfillmentText().contains("zh15002"));
        assertTrue(response.getFulfillmentText().contains("250"));
        assertTrue(response.getFulfillmentText().contains("Proyecto de tipo interno"));
        assertTrue(response.getFulfillmentText().contains("Luis Salazar"));
        assertTrue(response.getFulfillmentText().contains("Estado de los documentos requeridos"));
        assertTrue(response.getFulfillmentText().contains("entregado y sin aprobar"));
    }


    @Test
    void handle() {
        var params = Map.of("proyecto",(Object) "", "carnet", "not blank");
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        handler.handle(response, params);
        Mockito.verify(client, Mockito.never()).getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class));
    }

    private void setFields(String... data){
        ReflectionTestUtils.setField(handler, "baseUrl", data[0]);
        ReflectionTestUtils.setField(handler, "carnet", data[1]);
        ReflectionTestUtils.setField(handler, "projectName", data[2]);
    }
}