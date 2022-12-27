package io.coti.basenode.utilities;

import io.coti.basenode.services.HealthMetric;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "monitor")
public class MonitorConfigurationProperties {

    @Setter
    Map<String, Integer> warning;
    @Setter
    Map<String, Integer> critical;

    private void setThresholdValues(Map<String, Integer> thresholds, HealthMetric[] healthMetrics, String setThreshold) throws InvocationTargetException, IllegalAccessException {
        for (String metricName : thresholds.keySet()) {
            Optional<HealthMetric> optionalHealthMetric = Arrays.stream(healthMetrics).filter(p -> p.name().equals(metricName)).findFirst();
            if (optionalHealthMetric.isPresent()) {
                Integer thresholdValue = thresholds.get(metricName);
                Optional<Method> setMethod = Arrays.stream(optionalHealthMetric.get().getDeclaringClass().getDeclaredMethods()).filter(p -> p.getName().equals(setThreshold)).findFirst();
                if (setMethod.isPresent()) {
                    setMethod.get().invoke(optionalHealthMetric.get(), thresholdValue);
                } else {
                    throw new ValueException("Error while setting threshold! Health Metric " + metricName + " does not define method: " + setThreshold);
                }
            } else {
                throw new ValueException("Error while setting threshold! Health Metric " + metricName + " defined in properties does not exists!");
            }
        }
    }

    public void updateThresholds(@NotNull HealthMetric[] healthMetrics) throws InvocationTargetException, IllegalAccessException {
        setThresholdValues(warning, healthMetrics, "setWarningThreshold");
        setThresholdValues(critical, healthMetrics, "setCriticalThreshold");
    }
}
