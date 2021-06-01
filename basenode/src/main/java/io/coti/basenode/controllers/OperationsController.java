package io.coti.basenode.controllers;

import com.amazonaws.services.identitymanagement.model.InvalidInputException;
import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/operate")
public class OperationsController {

    @Value("${throttling.limit: 1}")
    private int throttlingLimit;

    @Throttling(type = ThrottlingType.RemoteAddr, limit = 1, timeUnit = TimeUnit.SECONDS)
    @GetMapping(path = "/alive")
    public boolean alive(){
        return true;
    }

    @PostMapping(path = "/UpdateThrottling")
    public boolean updateThrottling(@RequestParam(name = "className") String className, @RequestParam(name = "methodName") String methodName, @RequestParam(name = "throttlingValue", required = false, defaultValue = "-1") @Min(1)  Integer throttlingValue) throws InvalidInputException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {

        if (throttlingValue == -1)
            throttlingValue = throttlingLimit;

        Class<?> requiredClass = Class.forName(className);

        boolean methodFound = false;
        for (Method method : requiredClass.getDeclaredMethods())
        {
            if (method.getName().equals(methodName))
            {
                methodFound = true;
                method.setAccessible(true); //NOSONAR
                boolean throttlingAnnotationFound = false;
                for (Annotation annotation : method.getDeclaredAnnotations())
                {
                    Object handler = Proxy.getInvocationHandler(annotation);
                    if (com.weddini.throttling.Throttling.class.equals(annotation.annotationType()))
                    {
                        throttlingAnnotationFound = true;
                        Field memberValuesField = handler.getClass().getDeclaredField("memberValues");
                        memberValuesField.setAccessible(true); //NOSONAR
                        Map<String, Object> memberValues;
                        memberValues = (Map<String, Object>) memberValuesField.get(handler);
                        memberValues.put("limit", throttlingValue);
                        break;
                    }
                }
                if (! throttlingAnnotationFound)
                {
                    throw new InvalidInputException("Throttling Annotation not found for this method!");
                }
            }
        }
        if (! methodFound)
        {
            throw new NoSuchMethodException();
        }
        return true;
    }
}
