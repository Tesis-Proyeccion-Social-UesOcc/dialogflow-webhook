package sv.edu.ues.webhook.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import sv.edu.ues.webhook.services.Handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class Invoker {
    public Map<String, MethodData> methodDataMap = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void fulfill(String intentName) {
        var methodData = this.methodDataMap.get(intentName);
        if(methodData == null) return;

        var method = methodData.getMethod();
        ReflectionUtils.makeAccessible(method);
        try {
            method.invoke(methodData.getTarget());
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.warn("Something went wrong, cause: {}", e.getCause().getMessage());
        }
    }

    public void addMapping(Method method, Handler target, String intentName) {
        var data = new MethodData(method, target);
        this.methodDataMap.put(intentName, data);
    }
}
