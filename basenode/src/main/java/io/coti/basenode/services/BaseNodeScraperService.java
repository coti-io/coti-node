package io.coti.basenode.services;

import io.coti.basenode.data.HealthMetricOutput;
import io.coti.basenode.data.HealthState;
import io.coti.basenode.data.MetricClass;
import io.coti.basenode.services.interfaces.IScraperInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;

@Slf4j
@Service
public class BaseNodeScraperService implements IScraperInterface {

    private static final int MAX_NUMBER_OF_NON_FETCHED_ITERATIONS = 5;
    private static final String COMPONENT_TEMPLATE = "componentTemplate";
    private static final String METRIC_TEMPLATE = "metricTemplate";
    private final ArrayList<ArrayList<String>> metrics = new ArrayList<>();

    private final AtomicInteger numberOfNonFetchedIterations = new AtomicInteger(0);
    private final HashMap<String, String> metricTemplateMap = new HashMap<>();
    private String metricTemplate = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",metric=\"metricTemplate\"}";
    private String metricTemplateSubComponent = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",componentName=\"componentNameTemplate\",metric=\"metricTemplate\"}";
    private Thread sampleThread;
    @Value("${scraper.gather.metrics.millisec.interval:0}")
    private int metricToScraperInterval;
    @Value("${detailed.logs:false}")
    private boolean metricsDetailed;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public void initMonitor() {
        if (metricToScraperInterval == 0) {
            log.info("Not using scraper endpoint, {} initialization stopped...", this.getClass().getSimpleName());
            return;
        }
        if (metricToScraperInterval < 1000) {
            log.error("Scraper interval are too low (minimum 1000), {} initialization stopped...", this.getClass().getSimpleName());
            metricToScraperInterval = 0;
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
            String val = "";
            numberOfNonFetchedIterations.set(0);
            for (ArrayList<String> singleMetrics : metrics) {
                val = val.concat(String.join("\n", singleMetrics).concat("\n"));
            }
            metrics.clear();
            return val;
        }
    }

    public void getMetricsSample() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (metrics) {
                    lockAndGetSamples();
                }
                Thread.sleep(metricToScraperInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                log.error(String.valueOf(e1));
            }
        }
    }

    private void lockAndGetSamples() {
        try {
            monitorService.getMonitorReadWriteLock().readLock().lock();
            handleNonFetchedIterations();
            ArrayList<String> metricsLines = takeSample();
            metrics.add(metricsLines);
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            monitorService.getMonitorReadWriteLock().readLock().unlock();
        }
    }

    private ArrayList<String> takeSample() {
        ArrayList<String> metricsLines = new ArrayList<>();
        for (HealthMetric healthMetric : HealthMetric.values()) {

            for (HealthMetricOutput healthMetricOutput : healthMetric.getHealthMetricData().getAdditionalValues().values()) {
                if (HealthMetric.isToAddExternalMetric(healthMetricOutput.getType())) {
                    addMetric(healthMetric, healthMetricOutput.getLabel(),
                            healthMetricOutput.getValue(), metricsLines, metricTemplateMap);
                }
            }

            if (HealthMetric.isToAddExternalMetric(healthMetric.getHealthMetricOutputType())) {
                addMetric(healthMetric, healthMetric.getLabel(),
                        healthMetric.getHealthMetricData().getMetricValue(), metricsLines, metricTemplateMap);
            }
        }
        return metricsLines;
    }

    private void handleNonFetchedIterations() {
        if (numberOfNonFetchedIterations.incrementAndGet() > MAX_NUMBER_OF_NON_FETCHED_ITERATIONS) {
            metrics.remove(0);
            numberOfNonFetchedIterations.decrementAndGet();
        }
    }

    private void addMetric(HealthMetric healthMetric, String metricLabel, long metricValue, ArrayList<String> metrics,
                           HashMap<String, String> metricTemplateMap) {
        String metricTemplateVal = metricTemplateMap.get(healthMetric.getMetricClass().name());

        if (!healthMetric.getHealthMetricData().getLastHealthState().equals(HealthState.NA)) {
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
