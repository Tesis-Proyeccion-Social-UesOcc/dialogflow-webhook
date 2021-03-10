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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CertificadoServiceTest {


    @Mock
    private RestTemplate client;

    @InjectMocks
    private CertificadoService handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getExternalResourceUrl() {
        String base = "www.google.com/", carnet="zh15002", project="test";
        setFields(base, carnet, project);
        var result = handler.getExternalResourceUrl();

        assertEquals(base + "certificados/" + carnet + "?projectName="+project, result);

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
        var url = "www.example.com/some.pdf";
        var json = "{\"uri\": \""+url+"\"}";
        var toReturn = new ObjectMapper().readTree(json);
        Mockito.when(this.client.getForObject(Mockito.anyString(), ArgumentMatchers.eq(JsonNode.class))).thenReturn(toReturn);

        handler.externalCall(response);
        var messages = response.getFulfillmentMessages();
        var payload = messages.get(0).getPayload();
        var obj1 = (Map)((Map)payload.get("facebook")).get("attachment");
        var obj2 = (String)((Map)obj1.get("payload")).get("url");
        assertEquals(1, messages.size());
        assertEquals("\""+url+"\"", obj2);
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