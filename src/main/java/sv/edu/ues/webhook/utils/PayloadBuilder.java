package sv.edu.ues.webhook.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class PayloadBuilder {

    public static Map<String, Object> build(JsonNode url){
        return Map.of("facebook",
                    Map.of("attachment",
                            Map.of("type", "file", "payload",
                                    Map.of("url", url))));
    }

}
