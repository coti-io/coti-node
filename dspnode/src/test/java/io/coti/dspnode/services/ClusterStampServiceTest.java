package io.coti.dspnode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.GetClusterStampFileNamesCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.GetClusterStampFileNames;
import io.coti.basenode.model.*;
import io.coti.basenode.services.BaseNodeAwsService;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.TrustChainConfirmationService;
import io.coti.basenode.services.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ClusterStampService.class, ClusterStampNames.class, IDatabaseConnector.class,
        BaseNodeRocksDBConnector.class, ApplicationContext.class, BaseNodeNetworkService.class,
        GetClusterStampFileNamesCrypto.class, NodeCryptoHelper.class, BaseNodeAwsService.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ClusterStampServiceTest {

    @Autowired
    private ClusterStampService clusterStampService;
    @Autowired
    private ClusterStampNames clusterStampNames;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private GetClusterStampFileNamesCrypto getClusterStampFileNamesCrypto;
    @MockBean
    private BaseNodeNetworkService baseNodeNetworkService;

    //
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private TrustChainConfirmationService trustChainConfirmationService;
    @MockBean
    private Transactions transactions;
    @MockBean
    private Addresses addresses;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private TransactionVotes transactionVotes;
    @MockBean
    private NodeRegistrations nodeRegistrations;
    @MockBean
    private Currencies currencies;
    @MockBean
    private ClusterStampCrypto clusterStampCrypto;
    //

    public GetClusterStampFileNames getClusterStampFileNamesRequest = new GetClusterStampFileNames();
    public ClusterStampNameData major;
    public ClusterStampNameData token1;

    @Before
    public void setUp(){
        when(baseNodeNetworkService.getRecoveryServerAddress()).thenReturn("http://localhost:7040");
        databaseConnector.init();

        major = new ClusterStampNameData("clusterstamp_m_1565787205728.csv");
        major.setHash(new Hash("aaaaaaaaaaaaaaaa"));
        getClusterStampFileNamesRequest.setMajor(major.getName());

        List<String> tokens = new ArrayList<>();
        token1 = new ClusterStampNameData("clusterstamp_t_1565787205728_1565787268212.csv");
        token1.setHash(new Hash("aaaaaaaaaaaaaaab"));

        tokens.add(token1.getName());

        getClusterStampFileNamesRequest.setTokens(new HashSet<>(tokens));

        clusterStampNames.put(major);
        clusterStampNames.put(token1);
    }

    @After
    public void tearDown(){
        clusterStampNames.delete(major);
        clusterStampNames.delete(token1);
    }

    //TODO 8/14/2019 astolia: delete
    @Test
    public void printTime(){

        Instant instant = Instant.now();
        long timeStampMillis = instant.toEpochMilli();
        log.info("timestamp: {}", timeStampMillis);
        Assert.assertTrue(true);
    }

    @Test
    public void testMyFileStream(){
        clusterStampService.getClusterStampFromBackupNode();
    }

}
