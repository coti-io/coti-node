package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.GetClusterStampFileNamesCrypto;
import io.coti.basenode.model.LastClusterStampVersions;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IAwsService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.INetworkService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeClusterStampService.class, RestTemplate.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseNodeClusterStampServiceTest {

//    @Autowired
//    private BaseNodeClusterStampService clusterStampService;

//    @MockBean
//    RestTemplate restTemplate;

    //mock
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private TrustChainConfirmationService trustChainConfirmationService;
    @MockBean
    private Transactions transactions;
    @MockBean
    private ClusterStampCrypto clusterStampCrypto;
    @MockBean
    private GetClusterStampFileNamesCrypto getClusterStampFileNamesCrypto;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private IAwsService awsService;
    @MockBean
    private ApplicationContext applicationContext;
    @MockBean
    private LastClusterStampVersions lastClusterStampVersions;
    @MockBean
    private BaseNodeInitializationService baseNodeInitializationService;
    //

    String recoveryServerAddress = "http://localhost:7020";


    @Before
    public void beforeEachTest(){

        when(networkService.getRecoveryServerAddress()).thenReturn(recoveryServerAddress);
        prepareAndMockResponse();

    }

    private void prepareAndMockResponse(){
//        ClusterStampNameData major = new ClusterStampNameData("Clusterstamp_M_1565787205728_1565787834293.csv");
//        ClusterStampNameData token1 = new ClusterStampNameData("Clusterstamp_T_1565787205728_1565787268212.csv");
//        ClusterStampNameData token2 = new ClusterStampNameData("Clusterstamp_T_1565787205728_1565787834293.csv");
//        List<ClusterStampNameData> tokenClusterStampNames = Arrays.asList(token1, token2);
//        GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = new GetClusterStampFileNamesResponse();
//        getClusterStampFileNamesResponse.setMajor(major);
//        getClusterStampFileNamesResponse.setTokenClusterStampNames(tokenClusterStampNames);
//        ResponseEntity<GetClusterStampFileNamesResponse> requiredClusterStampNamesResponse = ResponseEntity.ok(getClusterStampFileNamesResponse);
        //when(restTemplate.getForEntity(recoveryServerAddress + "/clusterstamps", GetClusterStampFileNamesResponse.class)).thenReturn(requiredClusterStampNamesResponse);
    }

    //@Test
    public void getClusterStampFromRecoveryServer_Something_Something(){

        //baseNodeClusterStampService.getClusterStampFromRecoveryServer(true);
        Assert.assertTrue(true);
    }

//    private void mockNetworkService() {
//        NetworkNodeData networkNodeData = new NetworkNodeData();
//        networkNodeData.setAddress("localhost");
//        networkNodeData.setHttpPort("HISTORY_PORT");
//        Map<Hash, NetworkNodeData> networkNodeDataMap = new HashMap<>();
//        networkNodeDataMap.put(new Hash("aaaa"), networkNodeData);
//
//        when(networkService.getMapFromFactory(NodeType.HistoryNode)).thenReturn(networkNodeDataMap);
//    }

}
