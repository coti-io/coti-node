package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.crypto.GetNodeRegistrationRequestCrypto;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.*;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.*;
import io.coti.dspnode.data.UnconfirmedReceivedTransactionHashDspData;
import io.coti.dspnode.database.RocksDBConnector;
import io.coti.dspnode.model.UnconfirmedTransactionDspVotes;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


@Slf4j
@ContextConfiguration(classes = {BaseNodeTransactionPropagationCheckService.class,
        UnconfirmedReceivedTransactionHashes.class,
        RocksDBConnector.class,
})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest()
public class DspDoNotTakeItToDevTest {
    @MockBean
    protected Transactions transactions;
    @MockBean
    private ITransactionHelper transactionHelper;
    @Autowired
    private UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashes;
    @Autowired
    private RocksDBConnector rocksDBConnector;

    @MockBean
    private InitializationService initializationService;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private AddressService addressService;
    @MockBean
    private ICommunicationService communicationService;
    @MockBean
    private IReceiver messageReceiver;

    @MockBean
    protected INetworkService networkService;
    @MockBean
    private IAwsService awsService;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @MockBean
    private IDBRecoveryService dbRecoveryService;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private IMonitorService monitorService;
    @MockBean
    private IDspVoteService dspVoteService;
    @MockBean
    private IPotService potService;
    @MockBean
    private IPropagationSubscriber propagationSubscriber;
    @MockBean
    private IShutDownService shutDownService;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private GetNodeRegistrationRequestCrypto getNodeRegistrationRequestCrypto;
    @MockBean
    private NodeRegistrationCrypto nodeRegistrationCrypto;
    @MockBean
    private NetworkNodeCrypto networkNodeCrypto;
    @MockBean
    private NodeRegistrations nodeRegistrations;
    @MockBean
    private IClusterStampService clusterStampService;
    @MockBean
    private ITransactionSynchronizationService transactionSynchronizationService;
    @MockBean
    protected ApplicationContext applicationContext;
    @MockBean
    private BuildProperties buildProperties;
    @MockBean
    private ITransactionPropagationCheckService transactionPropagationCheckService;

    @MockBean
    private Addresses addresses;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private TransactionVotes transactionVotes;
    @MockBean
    private UnconfirmedTransactionDspVotes unconfirmedTransactionDspVotes;

    private static final Hash hash1 = new Hash("76d7f333480680c06df7d5c3cc17ffe2dc052597ae857285e57da249f4df344cf3e112739eca2aea63437f9e9819fac909ab93801b99853c779d8b6f5dcafb74");
    private static final Hash hash2 = new Hash("a2d27c3248e3530c55ca0941fd0fe5f419efcb6f923e54fe83ec5024040f86d107c6882f6a2435408964c2e9f522579248c8a983a2761a03ba253e7ca7898e53");
//    private static final Hash fakeNode3 = new Hash("0aa389aa3d8b31ecc5b2fa9164a0a2f52fb59165730de4527441b0278e5e47e51e3e1e69cf24a1a0bb58a53b262c185c4400f0d2f89b469c9498b6ed517b7398");
//    private static final Hash fakeNode4 = new Hash("e70a7477209fa59b3e866b33184ae47e5bed0d202c7214a4a93fd2592b11c3b567f2e85d28f3fc415401bb5a6b8be9eae5e77aa18d7e042c33ba91396d3cd970");
//    private static final Hash fakeNode5 = new Hash("5a4a7a8b72384bd6310135fdd939d1b105aec81a6ad72d745e5636770690a17c31eb6a775860b65b6211ec27d0690802032123a7f34f3acb68ed5d66366cd003");
//    private static final Hash fakeNode6 = new Hash("cd10ad2f479647dab74c0017958399a9ce87a56672bfd36739c70c4ddd2b2b5f451ff5deb10c86b745fcfa08dcb3ff1f331124bca608f5eab247ad1ec6e18281");
//    private static final Hash fakeNode7 = new Hash("b1fdc0efa64be6de413f888a3ed7aa4809d65c1a20b50ad775e9caeddaa1ae9c5027345157b1be789abd6961c410956be45da20de8444e874d8e487c6dfaa52b");


    @Test
    public void timeRocksDBCheck() {
        log.info("Test about to start");
    }

//    @Test
//    public void addEntryForUnconfirmedReceivedTransactionHashes() {
//        databaseConnector.init();
//
//        int retries = 3;
//        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData1 = new UnconfirmedReceivedTransactionHashData(hash1, retries, false);
//        unconfirmedReceivedTransactionHashes.put(unconfirmedReceivedTransactionHashData1);
//
//        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashesByHash1 = unconfirmedReceivedTransactionHashes.getByHash(hash1);
//        Assert.assertTrue(unconfirmedReceivedTransactionHashesByHash1 != null && unconfirmedReceivedTransactionHashesByHash1.getHash().equals(hash1));
//
//        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData2 = new UnconfirmedReceivedTransactionHashData(hash2, retries, false);
//        unconfirmedReceivedTransactionHashes.put(unconfirmedReceivedTransactionHashData2);
//
//        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashesByHash2 = unconfirmedReceivedTransactionHashes.getByHash(hash2);
//        Assert.assertTrue(unconfirmedReceivedTransactionHashesByHash2 != null && unconfirmedReceivedTransactionHashesByHash2.getHash().equals(hash2));
//    }

    @Test
    public void readEntryForUnconfirmedReceivedTransactionHashes() {
        databaseConnector.init();

        IEntity unconfirmedReceivedTransactionHashesByHash1 = unconfirmedReceivedTransactionHashes.getByHash(hash1);
        boolean dspVoteOnly = ((UnconfirmedReceivedTransactionHashDspData) unconfirmedReceivedTransactionHashesByHash1).isDSPVoteOnly();

//        UnconfirmedReceivedTransactionHashDspData unconfirmedReceivedTransactionHashesByHash1 = unconfirmedReceivedTransactionHashes.getByHash(hash1);
//        Assert.assertTrue(unconfirmedReceivedTransactionHashesByHash1 != null && unconfirmedReceivedTransactionHashesByHash1.getHash().equals(hash1));
//
//        UnconfirmedReceivedTransactionHashDspData unconfirmedReceivedTransactionHashesByHash2 = unconfirmedReceivedTransactionHashes.getByHash(hash2);
//        Assert.assertTrue(unconfirmedReceivedTransactionHashesByHash2 != null && unconfirmedReceivedTransactionHashesByHash2.getHash().equals(hash2));
    }

}