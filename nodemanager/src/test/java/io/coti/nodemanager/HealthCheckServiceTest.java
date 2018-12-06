package io.coti.nodemanager;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.services.HealthCheckService;
import io.coti.nodemanager.services.NodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;


//import static org.powermock.api.//org.powermock.api.mockito.PowerMockito.*;
//@ContextConfiguration(classes = {HealthCheckService.class , NodeManagementService.class, ActiveNode.class})
@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@Slf4j
@AutoConfigureMockMvc(secure = false)
public class HealthCheckServiceTest {
    private static final String NODE_HASH_URL = "http://localhost:8888/nodeHash";

    @Value("localhost")
    private String nodeManagerIp;

    @Value("8888")
    private String nodeManagerHttpPort;

    @Autowired
    private HealthCheckService healthCheckService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private NodeManagementService nodeManagementService;

    @MockBean
    private ActiveNode activeNode;

    @Autowired
    private INetworkDetailsService networkDetailsService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
//        this.nodeManagementService.init();
    }


    @Test
    public void testPositiveHealthCheck() {
        NetworkNodeData nodeToTest = new NetworkNodeData(NodeType.FullNode, nodeManagerIp, nodeManagerHttpPort, new Hash("1"));
        NodeManagementService nodeManagerServiceMock = spy(nodeManagementService);
        Hash nodeHash = new Hash("1");
        when(restTemplate.getForObject(NODE_HASH_URL, Hash.class)).thenReturn(nodeHash);
        doNothing().when(nodeManagerServiceMock).insertDeletedNodeRecord(any());
        networkDetailsService.addNode(nodeToTest);
        healthCheckService = new HealthCheckService(nodeManagerServiceMock, activeNode, restTemplate, networkDetailsService);
        healthCheckService.neighborsHealthCheck();
        Assert.assertTrue("Node was deleted from memory", networkDetailsService.isNodeExistsOnMemory(nodeToTest));
    }

    @Test
    public void testNegativeHealthCheck() {
        Hash nodeHash = new Hash("1");
        NodeManagementService nodeManagerServiceMock = spy(nodeManagementService);
        NetworkNodeData nodeToTest = new NetworkNodeData(NodeType.FullNode, nodeManagerIp, nodeManagerHttpPort, nodeHash);
        when(restTemplate.getForObject(NODE_HASH_URL, Hash.class)).thenReturn(null);
        doNothing().when(nodeManagerServiceMock).insertDeletedNodeRecord(any());
        networkDetailsService.addNode(nodeToTest);
        healthCheckService = new HealthCheckService(nodeManagerServiceMock, activeNode, restTemplate, networkDetailsService);
        healthCheckService.neighborsHealthCheck();
        Assert.assertTrue("Node was deleted from memory", !networkDetailsService.isNodeExistsOnMemory(nodeToTest));

    }


}
