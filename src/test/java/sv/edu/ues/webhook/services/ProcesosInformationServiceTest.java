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

import static org.junit.jupiter.api.Assertions.*;

class ProcesosInformationServiceTest {

    @Mock
    private RestTemplate client;

    @InjectMocks
    private ProcesosInformationService handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getExternalResourceUrl() {
        var url = "google.com/";
        ReflectionTestUtils.setField(handler, "baseUrl", url);
        assertEquals(url.concat("procesos"), handler.getExternalResourceUrl());
    }

    @Test
    void externalCall() throws JsonProcessingException {
        var url = "google.com/";
        ReflectionTestUtils.setField(handler, "baseUrl", url);
        var response = new GoogleCloudDialogflowV2WebhookResponse();
        var json = "{\"result\": [" +
                "{\"nombre\": \"name1\", \"descripcion\": \"desc\", \"fechaInicio\": \"2021-04-01\", \"fechaFin\": \"2021-04-15\"}" +
                "]}";
        var toReturn = new ObjectMapper().readTree(json);
        Mockito.when(this.client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(toReturn);
        handler.externalCall(response);

        var text = response.getFulfillmentText();
        assertTrue(text.contains("Calendarización de procesos:"));
        assertTrue(text.contains("NAME1"));
        assertTrue(text.contains("desc"));
        assertTrue(text.contains("Calendarizacion de entregas"));
        assertTrue(text.contains("2021-04-01 / 2021-04-15"));
        assertTrue(text.contains("Los documentos deben presentarse en PDF"));
    }
}