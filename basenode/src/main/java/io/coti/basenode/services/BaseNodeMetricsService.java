package io.coti.basenode.services;

import io.coti.basenode.data.HealthMetricOutput;
import io.coti.basenode.data.HealthMetricOutputType;
import io.coti.basenode.data.MetricClass;
import io.coti.basenode.services.interfaces.IMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class BaseNodeMetricsService implements IMetricsService {

    private static final int MAX_NUMBER_OF_NON_FETCHED_SAMPLES = 50;
    private static final String COMPONENT_TEMPLATE = "componentTemplate";
    private static final String METRIC_TEMPLATE = "metricTemplate";
    private final ArrayList<String> metrics = new ArrayList<>();
    private final AtomicInteger numberOfNonFetchedSamples = new AtomicInteger(0);
    private final HashMap<String, String> metricTemplateMap = new HashMap<>();
    private String metricTemplate = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",metric=\"metricTemplate\"}";
    private String metricTemplateSubComponent = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",componentName=\"componentNameTemplate\",metric=\"metricTemplate\"}";
    private Thread sampleThread;
    @Autowired
    private BaseNodeMonitorService baseNodeMonitorService;
    @Value("${metrics.sample.milisec.interval:0}")
    private int metricsSampleInterval;
    @Value("${detailed.logs:false}")
    private boolean metricsDetailed;

    public void init() {
        if (metricsSampleInterval == 0) {
            log.info("Not using metrics endpoint, {} initialization stopped...", this.getClass().getSimpleName());
            return;
        }
        if (metricsSampleInterval < 1000) {
            log.error("Metrics samples are too low (minimum 1000), {} initialization stopped...", this.getClass().getSimpleName());
            metricsSampleInterval = 0;
            return;
        }
        String hostName = "unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error(e.toString());
        }
        metricTemplate = metricTemplate.replace("nodeTemplate", hostName);
        metricTemplateSubComponent = metricTemplateSubComponent.replace("nodeTemplate", hostName);

        metricTemplateMap.put(MetricClass.QUEUE_METRIC.name(), metricTemplate.replace(COMPONENT_TEMPLATE, "queues"));
        metricTemplateMap.put(MetricClass.TRANSACTIONS_METRIC.name(), metricTemplate.replace(COMPONENT_TEMPLATE, "transactions"));
        metricTemplateMap.put(MetricClass.BACKUP_METRIC.name(), metricTemplateSubComponent.replace(COMPONENT_TEMPLATE, "backups"));
        metricTemplateMap.put(MetricClass.DATABASE_METRIC.name(), metricTemplate.replace(COMPONENT_TEMPLATE, "database"));
        metricTemplateMap.put(MetricClass.SYSTEM_METRIC.name(), metricTemplate.replace(COMPONENT_TEMPLATE, "system"));

        sampleThread = new Thread(this::getMetricsSample, "MetricsSample");
        sampleThread.start();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public String getMetrics(HttpServletRequest request) {
        if (sampleThread == null || !sampleThread.isAlive()) {
            log.error("Metrics sample thread not initialized!, returning null to {}...", request.getRemoteAddr());
            return null;
        }
        synchronized (metrics) {
            numberOfNonFetchedSamples.set(0);
            String val = String.join("\n", metrics).concat("\n");
            metrics.clear();
            return val;
        }
    }

    public void getMetricsSample() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (metrics) {
                    if (numberOfNonFetchedSamples.incrementAndGet() > MAX_NUMBER_OF_NON_FETCHED_SAMPLES) {
                        metrics.clear();
                    }
                    for (HealthMetric healthMetric : HealthMetric.values()) {

                        for (HealthMetricOutput healthMetricOutput : healthMetric.getHealthMetricData().getAdditionalValues().values()) {
                            if (healthMetric.getHealthMetricOutputType().equals(HealthMetricOutputType.ALL) ||
                                    healthMetric.getHealthMetricOutputType().equals(HealthMetricOutputType.EXTERNAL)) {
                                addMetric(healthMetric, healthMetricOutput.getLabel(),
                                        healthMetricOutput.getValue(), metrics, metricTemplateMap);
                            }
                        }

                        if (healthMetric.getHealthMetricOutputType().equals(HealthMetricOutputType.ALL) ||
                                healthMetric.getHealthMetricOutputType().equals(HealthMetricOutputType.EXTERNAL)) {
                            addMetric(healthMetric, healthMetric.getLabel(),
                                    healthMetric.getHealthMetricData().getMetricValue(), metrics, metricTemplateMap);
                        }
                    }
                }
                Thread.sleep(metricsSampleInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                log.error(String.valueOf(e1));
            }
        }
    }

    private void addMetric(HealthMetric healthMetric, String metricLabel, long metricValue, ArrayList<String> metrics,
                           HashMap<String, String> metricTemplateMap) {
        String metricTemplateVal = metricTemplateMap.get(healthMetric.getMetricClass().name());

        if (!healthMetric.getHealthMetricData().getLastHealthState().equals(BaseNodeMonitorService.HealthState.NA)) {
            if (metricsDetailed || !healthMetric.isDetailedLogs()) {
                addMetric(metrics, metricTemplateVal, healthMetric, metricLabel, metricValue);
            }
            addMetric(metrics, metricTemplateVal, healthMetric, metricLabel + "State", healthMetric.getHealthMetricData().getLastHealthState().ordinal());
        }
    }

    private void addMetric(ArrayList<String> metrics, String metricTemplate, HealthMetric healthMetric, String healthMetricLabel, long metricValue) {
        long snapshotTime = healthMetric.getHealthMetricData().getSnapshotTime().toEpochMilli();
        metrics.add(metricTemplate.replace(METRIC_TEMPLATE, healthMetricLabel)
                .concat(" ").concat(String.valueOf(metricValue)).concat(" ").concat(String.valueOf(snapshotTime)));
    }

}
