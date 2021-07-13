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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class BaseNodeMetricsService implements IMetricsService {

    private static final int MAX_NUMBER_OF_NON_FETCHED_SAMPLES = 50;
    @Autowired
    IReceiver receiver;
    @Autowired
    IPropagationPublisher propagationPublisher;
    String metricTemplate = "coti_node{host=\"nodeTemplate\",components=\"componentTemplate\",metric=\"metricTemplate\"} num timestamp";
    String metricQueuesTemplate;
    String metricTransactionsTemplate;
    final ArrayList<String> metrics = new ArrayList<>();
    Thread sampleThread;
    AtomicInteger numberOfNonFetchedSamples = new AtomicInteger(0);
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
    @Value("${metrics.sample.milisec.interval:0}")
    private String metricsSampleInterval;

    public void init() {
        if (Integer.parseInt(metricsSampleInterval) == 0) {
            log.info("Not using metrics endpoint, {} initialization stopped...", this.getClass().getSimpleName());
            return;
        }
        if (Integer.parseInt(metricsSampleInterval) < 1000) {
            log.error("Metrics samples are too low (minimum 1000), {} initialization stopped...", this.getClass().getSimpleName());
            metricsSampleInterval = "0";
            return;
        }
        String hostName = "unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error(e.toString());
        }
        metricTemplate = metricTemplate.replace("nodeTemplate", hostName);
        metricQueuesTemplate = metricTemplate.replace("componentTemplate", "queues");
        metricTransactionsTemplate = metricTemplate.replace("componentTemplate", "transactions");

        sampleThread = new Thread(this::sample, "MetricsSample");
        sampleThread.start();
        log.info("{} is up", this.getClass().getSimpleName());
    }


    @Override
    public String getMetrics(HttpServletRequest request)  {
        if ( sampleThread == null || ! sampleThread.isAlive()) {
            log.error("metrics sample thread not initialized!, returning null to {}...", request.getRemoteAddr());
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
        metrics.add(metricQueuesTemplate.replace("metricTemplate", queueMetric)
                .replace("num", String.valueOf(value)).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));
    }

    private void addTransaction(String transactionMetric, long value) {
        metrics.add(metricTransactionsTemplate.replace("metricTemplate", transactionMetric)
                .replace("num", String.valueOf(value)).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));
    }

    public void sample() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (metrics) {
                if (numberOfNonFetchedSamples.incrementAndGet() > MAX_NUMBER_OF_NON_FETCHED_SAMPLES) {
                    metrics.clear();
                }
                addQueue("ZeroMQReceiver", receiver.getQueueSize());
                addQueue("PropagationPublisher", propagationPublisher.getQueueSize());

                Map<String, String> maps = propagationSubscriber.getQueueSize();
                for (Map.Entry<String, String> entry : maps.entrySet())
                {
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
            }
            try {
                Thread.sleep(Integer.parseInt(metricsSampleInterval));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
