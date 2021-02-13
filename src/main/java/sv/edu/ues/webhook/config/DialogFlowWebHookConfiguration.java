package sv.edu.ues.webhook.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import sv.edu.ues.webhook.annotations.IntentHandlerScanner;

@Configuration
public class DialogFlowWebHookConfiguration {

    @Bean
    public RestTemplate templateBuilder(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public IntentHandlerScanner annotationScanner(){
        return new IntentHandlerScanner();
    }
}
