package io.coti.basenode.utilities;

import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.services.HealthMetric;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Optional;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.SET_CRITICAL_THRESHOLD;
import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.SET_WARNING_THRESHOLD;

@ConfigurationProperties(prefix = "monitor")
@RefreshScope
public class MonitorConfigurationProperties {

    @Setter
    Map<String, Integer> warning;
    @Setter
    Map<String, Integer> critical;

    private void setThresholdValues(Map<String, Integer> thresholds, Map<HealthMetric, HealthMetricData> healthMetrics, String setThreshold) throws InvocationTargetException, IllegalAccessException {
        if (thresholds == null) {
            return;
        }

        for (Map.Entry<String, Integer> metricEntry : thresholds.entrySet()) {
            Optional<HealthMetric> optionalHealthMetric = healthMetrics.keySet().stream().filter(p -> p.name().equals(metricEntry.getKey())).findFirst();
            if (optionalHealthMetric.isPresent()) {
                HealthMetricData healthMetricData = healthMetrics.get(optionalHealthMetric.get());
                Optional<Method> setMethod = Arrays.stream(healthMetricData.getClass().getDeclaredMethods()).filter(p -> p.getName().equals(setThreshold)).findFirst();
                if (setMethod.isPresent()) {
                    setMethod.get().invoke(healthMetricData, Long.valueOf(metricEntry.getValue()));
                } else {
                    throw new InputMismatchException("Error while setting threshold! Health Metric " + metricEntry.getKey() + " does not define method: " + setThreshold);
                }
            } else {
                throw new InputMismatchException("Error while setting threshold! Health Metric " + metricEntry.getKey() + " defined in properties does not exists!");
            }
        }
    }

    public void updateThresholds(Map<HealthMetric, HealthMetricData> healthMetrics) throws InvocationTargetException, IllegalAccessException {
        setThresholdValues(warning, healthMetrics, SET_WARNING_THRESHOLD);
        setThresholdValues(critical, healthMetrics, SET_CRITICAL_THRESHOLD);
    }
}
