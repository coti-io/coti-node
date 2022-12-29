package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.RejectedTransactions;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utilities.MonitorConfigurationProperties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class BaseNodeServiceManager {

    @Autowired
    private INetworkService networkService;
    @Autowired
    private MonitorConfigurationProperties monitorConfigurationProperties;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private RejectedTransactions rejectedTransactions;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IWebSocketMessageService webSocketMessageService;
    @Autowired
    private IReceiver receiver;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private IDBRecoveryService dbRecoveryService;
}
