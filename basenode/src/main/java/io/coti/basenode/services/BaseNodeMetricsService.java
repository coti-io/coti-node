package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BaseNodeMetricsService implements IMetricsService {

    @Autowired
    IReceiver receiver;
    @Autowired
    IPropagationPublisher propagationPublisher;
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

    char quotes ='"';

    String metric_template = "coti_node{host=\"nodeTemplate\";components=\"componentTemplate\";metric=\"metricTemplate\"} num timestamp";
    String metric_queues_template;
    String metric_transactions_template;

    ArrayList<String> metrics = new ArrayList<String>();

    public void init() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        metric_template = metric_template.replace("nodeTemplate", hostName);
        metric_queues_template = metric_template.replace("componentTemplate", "queues");
        metric_transactions_template = metric_template.replace("componentTemplate", "transactions");
    }

    @Override
    public String getMetrics() {
        synchronized (metrics)
        {
            String val = metrics.toString().replace("[","").replace("]","\n")
                    .replace(", ","\n").replaceAll(";", ",");
            metrics.clear();
            return val;
        }
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 500)
    public void sample(){

        synchronized (metrics)
        {
            metrics.add(metric_transactions_template.replace("metricTemplate","Total")
                    .replace("num", String.valueOf(transactionHelper.getTotalTransactions())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_queues_template.replace("metricTemplate","ZeroMQReceiver")
                    .replace("num", String.valueOf(receiver.getQueueCount())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_queues_template.replace("metricTemplate","PropagationPublisher")
                    .replace("num", String.valueOf(propagationPublisher.getQueueCount())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            Map<String,String> maps = propagationSubscriber.getQueueCount();
            for (String key : maps.keySet())
            {
                metrics.add(metric_queues_template.replace("metricTemplate","PropagationSubscriber_" + key)
                        .replace("num", maps.get(key)).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            }

            metrics.add(metric_queues_template.replace("metricTemplate","Confirmations")
                    .replace("num", String.valueOf(confirmationService.getConfirmationQueue())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","WaitingDspConsensusResultsConfirmed")
                    .replace("num", String.valueOf(confirmationService.getWaitingDspConsensusResultsMapSize())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","WaitingMissingTransactionIndexes")
                    .replace("num", String.valueOf(confirmationService.getWaitingMissingTransactionIndexes())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","TrustChainConfirmed")
                    .replace("num", String.valueOf(confirmationService.getTrustChainConfirmed())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","DspConfirmed")
                    .replace("num", String.valueOf(confirmationService.getDspConfirmed())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","TotalConfirmed")
                    .replace("num", String.valueOf(confirmationService.getTotalConfirmed())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","Index")
                    .replace("num", String.valueOf(transactionIndexService.getLastTransactionIndexData().getIndex())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","Sources")
                    .replace("num", String.valueOf(clusterService.getTotalSources())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_transactions_template.replace("metricTemplate","TotalPostponedTransactions")
                    .replace("num", String.valueOf(transactionService.totalPostponedTransactions())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

//            metrics.add(metric_queues_template.replace("metricTemplate","ZeroMQSubscriberQueue_Transaction")
//                    .replace("num", String.valueOf(propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.TRANSACTION))).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

            metrics.add(metric_queues_template.replace("metricTemplate","WebSocketMessages")
                    .replace("num", String.valueOf(webSocketMessageService.getMessageQueueSize())).replace("timestamp", String.valueOf(Instant.now().toEpochMilli())));

        }

    }
}
