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
        if (thresholds == null) {
            return;
        }
        for (Map.Entry<String, Integer> metricEntry : thresholds.entrySet()) {
            Optional<HealthMetric> optionalHealthMetric = Arrays.stream(healthMetrics).filter(p -> p.name().equals(metricEntry.getKey())).findFirst();
            if (optionalHealthMetric.isPresent()) {
                Optional<Method> setMethod = Arrays.stream(optionalHealthMetric.get().getDeclaringClass().getDeclaredMethods()).filter(p -> p.getName().equals(setThreshold)).findFirst();
                if (setMethod.isPresent()) {
                    setMethod.get().invoke(optionalHealthMetric.get(), metricEntry.getValue());
                } else {
                    throw new ValueException("Error while setting threshold! Health Metric " + metricEntry.getKey() + " does not define method: " + setThreshold);
                }
            } else {
                throw new ValueException("Error while setting threshold! Health Metric " + metricEntry.getKey() + " defined in properties does not exists!");
            }
        }
    }

    public void updateThresholds(@NotNull HealthMetric[] healthMetrics) throws InvocationTargetException, IllegalAccessException {
        setThresholdValues(warning, healthMetrics, "setWarningThreshold");
        setThresholdValues(critical, healthMetrics, "setCriticalThreshold");
    }
}
