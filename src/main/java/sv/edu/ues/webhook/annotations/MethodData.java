package sv.edu.ues.webhook.annotations;

import sv.edu.ues.webhook.services.Handler;

import java.lang.reflect.Method;

/**
 * Class to act as a container for methods annotated with {@link IntentHandler} and its container object
 */
public class MethodData {
    private final Method method;
    private final Handler target;

    public MethodData(Method method, Handler target) {
        this.method = method;
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public Handler getTarget() {
        return target;
    }
}
