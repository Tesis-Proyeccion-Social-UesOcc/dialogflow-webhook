package sv.edu.ues.webhook.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import sv.edu.ues.webhook.services.Handler;

public class IntentHandlerScanner implements BeanPostProcessor, ApplicationContextAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        // only looking for component that are intent handlers, otherwise, skip
        if(!(bean instanceof Handler)){
            return bean;
        }
        var currentBeanClass = bean.getClass();

        MethodIntrospector.MetadataLookup<IntentHandler> metadataLookup =
                method -> AnnotatedElementUtils.getMergedAnnotation(method, IntentHandler.class);

        var annotatedMethods = MethodIntrospector.selectMethods(currentBeanClass, metadataLookup);

        if(annotatedMethods.isEmpty()){
            logger.warn("No @IntentHandler annotations found on current handler: {}", currentBeanClass.getSimpleName());
        }
        else{
            var invoker = this.applicationContext.getBean(Invoker.class);
            for(var entry: annotatedMethods.entrySet()){
                invoker.addMapping(entry.getKey(), (Handler) bean, entry.getValue().value());
            }
        }
        return bean;
    }

}
