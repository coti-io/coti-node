package io.coti.basenode.services;


import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.communication.ZeroMQPropagationPublisher;
import io.coti.basenode.communication.ZeroMQSubscriber;
import io.coti.basenode.communication.interfaces.ISubscriberHandler;
import io.coti.basenode.crypto.*;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicLong;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration
        (classes = {
                Addresses.class,
                NodeCryptoHelper.class,
                BaseNodeRocksDBConnector.class,
                BaseNodeConfirmationService.class,
                LiveViewService.class,
                BaseNodeBalanceService.class,
                TransactionHelper.class,
                TransactionIndexService.class,
                Transactions.class,
                AddressTransactionsHistories.class,
                ClusterService.class,
                BaseNodeMonitorService.class,
                BaseNodeTransactionService.class,
                BaseNodeAddressService.class,
                BaseNodeDspVoteService.class,
                BaseNodePotService.class,
                ZeroMQSubscriber.class,
                SimpMessagingTemplate.class,
                TransactionCrypto.class,
                TransactionSenderCrypto.class,
                SourceSelector.class,
                TccConfirmationService.class,
                TransactionIndexes.class,
                DspConsensusCrypto.class,
                TransactionTrustScoreCrypto.class,
                ValidationService.class,
                ZeroMQPropagationPublisher.class,
                JacksonSerializer.class,
                BaseNodeMonitorService.class,
                ISubscriberHandler.class}
        )
@Slf4j
public class BaseNodeMonitorServiceTest {

    @Autowired
    private BaseNodeMonitorService baseNodeMonitorService;
    @Autowired
    private JacksonSerializer jacksonSerializer;
    @Autowired
    private ZeroMQPropagationPublisher zeroMQPropagationPublisher;
    @Autowired
    private Addresses Addresses;
    @Autowired
    private ValidationService ValidationService;
    @Autowired
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;
    @Autowired
    private DspConsensusCrypto dspConsensusCrypto;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private TccConfirmationService tccConfirmationService;
    @Autowired
    private SourceSelector sourceSelector;
    @Autowired
    private BaseNodeConfirmationService baseNodeConfirmationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IMonitorService monitorService;
    @Autowired
    private LiveViewService liveViewService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IDspVoteService dspVoteService;
    @Autowired
    private IPotService potService;
    @Autowired
    private ZeroMQSubscriber zeroMQSubscriber;
    @Autowired
    private IDatabaseConnector rocksDbConnector;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;
    @MockBean
    private ISubscriberHandler subscriberHandler;

    @Test
    public void lastState_noExceptionIsThrown() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        AtomicLong maxTransactionIndex = new AtomicLong(-1);
        try {
            transactionIndexService.init(maxTransactionIndex);
            baseNodeMonitorService.lastState();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
