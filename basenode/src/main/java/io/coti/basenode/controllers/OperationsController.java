package io.coti.basenode.controllers;

import com.sun.corba.se.impl.corba.EnvironmentImpl;
import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.instrument.InstrumentationImpl;
import sun.management.counter.perf.InstrumentationException;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
    public boolean UpdateThrottling(String className, String methodName, Integer ThrottlingValue) throws Exception {

        if (ThrottlingValue == null || ThrottlingValue == -1)
        {
            ThrottlingValue = throttlingLimit;
        }
        Class requiredClass = null;
        try {
            requiredClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw e;
        }

        boolean methodFound = false;
        for (Method method : requiredClass.getDeclaredMethods())
        {
            if (method.getName().equals(methodName))
            {
                methodFound = true;
                method.setAccessible(true);
                boolean throttlingAnnotationFound = false;
                for (Annotation annotation : method.getDeclaredAnnotations())
                {
                    Object handler = Proxy.getInvocationHandler(annotation);
                    if (com.weddini.throttling.Throttling.class.equals(annotation.annotationType()))
                    {
                        throttlingAnnotationFound = true;
                        Field memberValuesField = handler.getClass().getDeclaredField("memberValues");
                        memberValuesField.setAccessible(true);
                        Map<String, Object> memberValues = (Map<String, Object>) memberValuesField.get(handler);
                        memberValues.put("limit", ThrottlingValue);
                        break;
                    }
                }
                if (! throttlingAnnotationFound)
                {
                    throw new Exception("Throttling Annotation not found for this method!");
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
