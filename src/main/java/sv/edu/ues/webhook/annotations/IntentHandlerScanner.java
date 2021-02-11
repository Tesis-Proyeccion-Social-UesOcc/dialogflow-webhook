package sv.edu.ues.webhook.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import sv.edu.ues.webhook.services.Handler;
import sv.edu.ues.webhook.annotations.IntentHandler;

public class IntentHandlerScanner implements BeanPostProcessor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if(!(bean instanceof Handler)){
            return bean;
        }
        var currentBeanClass = bean.getClass();

        MethodIntrospector.MetadataLookup<IntentHandler> metadataLookup =
                method -> AnnotatedElementUtils.getMergedAnnotation(method, IntentHandler.class);

        var annotatedMethods = MethodIntrospector.selectMethods(currentBeanClass, metadataLookup);

        if(annotatedMethods.isEmpty()){
            logger.warn("No @IntentHandler annotations found on current component: {}", currentBeanClass.getSimpleName());
        }
        else{
            logger.info(String.valueOf(annotatedMethods));
        }
        return bean;
    }

}
