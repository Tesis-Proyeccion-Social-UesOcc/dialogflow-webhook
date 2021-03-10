package sv.edu.ues.webhook.utils;

import java.util.Map;

public class PayloadBuilder {

    public static Map<String, Object> build(String url){
        return Map.of("facebook",
                    Map.of("attachment",
                            Map.of("type", "file", "payload",
                                    Map.of("url", url))));
    }

}
