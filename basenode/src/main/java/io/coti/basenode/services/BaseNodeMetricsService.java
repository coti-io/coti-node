package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class BaseNodeMetricsService implements IMetricsService {

    public static final int MAX_NUMBER_OF_NON_FETCHED_SAMPLES = 50;
    private static final String COMPONENT_TEMPLATE = "componentTemplate";
    private static final String METRIC_TEMPLATE = "metricTemplate";
    private final ArrayList<String> metrics = new ArrayList<>();
    @Autowired
    private IReceiver receiver;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    private String metricTemplate = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",metric=\"metricTemplate\"}";
    private String metricTemplateSubComponent = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",componentName=\"componentNameTemplate\",metric=\"metricTemplate\"}";
    private String metricQueuesTemplate;
    private String metricTransactionsTemplate;
    private String metricBackupsTemplate;
    private Thread sampleThread;
    private final AtomicInteger numberOfNonFetchedSamples = new AtomicInteger(0);
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IWebSocketMessageService webSocketMessageService;
    @Autowired
    private IDBRecoveryService dbRecoveryService;
    @Autowired
    private ICommunicationService communicationService;
    @Value("${metrics.sample.milisec.interval:0}")
    private int metricsSampleInterval;

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

        metricQueuesTemplate = metricTemplate.replace(COMPONENT_TEMPLATE, "queues");
        metricTransactionsTemplate = metricTemplate.replace(COMPONENT_TEMPLATE, "transactions");
        metricBackupsTemplate = metricTemplateSubComponent.replace(COMPONENT_TEMPLATE, "backups");

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

    private void addQueue(String queueMetric, long value) {
        metrics.add(metricQueuesTemplate.replace(METRIC_TEMPLATE, queueMetric)
                .concat(" ").concat(String.valueOf(value)).concat(" ").concat(String.valueOf(Instant.now().toEpochMilli())));
    }

    private void addTransaction(String transactionMetric, long value) {
        metrics.add(metricTransactionsTemplate.replace(METRIC_TEMPLATE, transactionMetric)
                .concat(" ").concat(String.valueOf(value)).concat(" ").concat(String.valueOf(Instant.now().toEpochMilli())));
    }

    private void addBackup(String backupMetric, String backupName, long value) {
        metrics.add(metricBackupsTemplate.replace(METRIC_TEMPLATE, backupMetric).replace("componentNameTemplate", backupName)
                .concat(" ").concat(String.valueOf(value)).concat(" ").concat(String.valueOf(Instant.now().toEpochMilli())));
    }

    private void addBackups() {
        HashMap<String, HashMap<String, Long>> backupLog = dbRecoveryService.getBackUpLog();
        for (Map.Entry<String, HashMap<String, Long>> entry : backupLog.entrySet()) {
            String backupName = entry.getKey();
            for (Map.Entry<String, Long> subEntry : entry.getValue().entrySet()) {
                String backupMetric = subEntry.getKey();
                long value = subEntry.getValue();
                addBackup(backupMetric, backupName, value);
            }
        }
        if (backupLog.size() > 0)
            dbRecoveryService.clearBackupLog();
    }

    public void getMetricsSample() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (metrics) {
                if (numberOfNonFetchedSamples.incrementAndGet() > MAX_NUMBER_OF_NON_FETCHED_SAMPLES) {
                    metrics.clear();
                }
                addQueue("ZeroMQReceiver", receiver.getQueueSize());
                addQueue("PropagationPublisher", propagationPublisher.getQueueSize());

                Map<String, String> maps = propagationSubscriber.getQueueSizeMap();
                for (Map.Entry<String, String> entry : maps.entrySet()) {
                    addQueue("PropagationSubscriber_" + entry.getKey(), Integer.parseInt(entry.getValue()));
                }

                addQueue("Confirmations", confirmationService.getQueueSize());
                addQueue("WebSocketMessages", webSocketMessageService.getMessageQueueSize());
                addTransaction("Total", transactionHelper.getTotalTransactions());
                addTransaction("WaitingDspConsensusResultsConfirmed", confirmationService.getWaitingDspConsensusResultsMapSize());
                addTransaction("WaitingMissingTransactionIndexes", confirmationService.getWaitingMissingTransactionIndexesSize());
                addTransaction("TrustChainConfirmed", confirmationService.getTrustChainConfirmed());
                addTransaction("DspConfirmed", confirmationService.getDspConfirmed());
                addTransaction("TotalConfirmed", confirmationService.getTotalConfirmed());
                addTransaction("Index", transactionIndexService.getLastTransactionIndexData().getIndex());
                addTransaction("Sources", clusterService.getTotalSources());
                addTransaction("TotalPostponedTransactions", transactionService.totalPostponedTransactions());
                addTransaction("invalidSenders", communicationService.resetHistoricInvalidSendersSize());

                addBackups();
            }
            try {
                Thread.sleep(metricsSampleInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
