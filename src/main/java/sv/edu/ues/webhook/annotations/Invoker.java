package sv.edu.ues.webhook.annotations;

import org.springframework.stereotype.Component;
import sv.edu.ues.webhook.services.Handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class Invoker {
    public Map<String, MethodData> methodDataMap = new HashMap<>();

    public void addMapping(Method method, Handler target, String intentName){
        var data = new MethodData(method, target);
        this.methodDataMap.put(intentName, data);
    }
}
