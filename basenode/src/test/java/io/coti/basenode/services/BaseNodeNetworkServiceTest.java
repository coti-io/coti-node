package io.coti.basenode.services;


import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ICommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;

import static org.mockito.Mockito.when;
import static testUtils.BaseNodeTestUtils.generateRandomHash;
import static testUtils.BaseNodeTestUtils.generateRandomNetworkNodeData;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration
        (classes = {
                BaseNodeNetworkService.class,
        }
        )
@Slf4j
public class BaseNodeNetworkServiceTest {

    private static final String NEW_ADDRESS = "newAddress";
    private static final String NEW_PORT = "8888";
    @Autowired
    private BaseNodeNetworkService baseNodeNetworkService;
    @MockBean
    private ICommunicationService communicationService;
    @MockBean
    private NetworkNodeCrypto networkNodeCrypto;
    @MockBean
    private NodeRegistrationCrypto nodeRegistrationCrypto;


    @Before
    public void setUp() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        baseNodeNetworkService.init();
    }

    @Test
    public void lastState() {
        Assert.assertNotNull(baseNodeNetworkService.multipleNodeMaps);
        baseNodeNetworkService.lastState();
    }

    @Test
    public void getMapFromFactory_retrieveMultipleNodeTypeMap_noExceptionIsThrown() {

        try {
            NodeTypeService.getNodeTypeList(true).forEach(nodeType -> {
                baseNodeNetworkService.getMapFromFactory(nodeType);
            });
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMapFromFactory_retrieveSingleNodeTypeMap_exceptionIsThrown() {
        NodeTypeService.getNodeTypeList(false).forEach(nodeType -> {
            baseNodeNetworkService.getMapFromFactory(nodeType);
        });
    }

    @Test
    public void getSingleNodeData_retrieveSingleNodeTypes_noExceptionIsThrown() {
        NodeTypeService.getNodeTypeList(false).forEach(nodeType -> baseNodeNetworkService.getSingleNodeData(nodeType));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSingleNodeData_retrieveMultipleNodeTypes_exceptionIsThrown() {
        NodeTypeService.getNodeTypeList(true).forEach(nodeType -> baseNodeNetworkService.getSingleNodeData(nodeType));
    }


    @Test
    public void addNode_invalidNetworkNodeDataRequest_requestNotUpdated() {
        // For Single Node Type
        boolean isMultipleNodeType = false;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        networkNodeData.setHash(null); // Invalidating data
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertEquals(null, baseNodeNetworkService.getSingleNodeData( NodeTypeService.getNodeTypeList(false).get(0)));

        // For Multiple Node Type
        isMultipleNodeType = true;
        networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        networkNodeData.setNodeType(null); // Invalidating data
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertTrue(baseNodeNetworkService.multipleNodeMaps.get(NodeTypeService.getNodeTypeList(true).get(0)).isEmpty());
    }

    @Test
    public void addNode_singleAndMultipleValidNetworkNodeDataRequest_requestUpdated() {
        // For Single Node Type
        boolean isMultipleNodeType = false;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertEquals(baseNodeNetworkService.getSingleNodeData( NodeTypeService.getNodeTypeList(false).get(0)), networkNodeData);

        // For Multiple Node Type
        isMultipleNodeType = true;
        networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertEquals(baseNodeNetworkService.multipleNodeMaps.get(NodeTypeService.getNodeTypeList(true).get(0)).get(networkNodeData.getHash()), networkNodeData);
    }


    @Test
    public void removeNode_singleAndMultipleValidNetworkNodeData_requestUpdated() {
        // For Single Node Type
        boolean isMultipleNodeType = false;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertEquals(baseNodeNetworkService.getSingleNodeData( NodeTypeService.getNodeTypeList(false).get(0)), networkNodeData);
        baseNodeNetworkService.removeNode(networkNodeData);
        Assert.assertEquals(null, baseNodeNetworkService.getSingleNodeData(  NodeTypeService.getNodeTypeList(false).get(0)));

        // For Multiple Node Type
        isMultipleNodeType = true;
        networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertEquals(baseNodeNetworkService.multipleNodeMaps.get(NodeTypeService.getNodeTypeList(true).get(0)).get(networkNodeData.getHash()), networkNodeData);
        baseNodeNetworkService.removeNode(networkNodeData);
        Assert.assertTrue(baseNodeNetworkService.multipleNodeMaps.get(NodeTypeService.getNodeTypeList(true).get(0)).isEmpty());
    }


    @Test
    public void updateNetworkNode_singleAndMultipleValidNetworkNodeData_requestUpdated() {
        // For Single Node Type
        boolean isMultipleNodeType = false;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        Assert.assertFalse(baseNodeNetworkService.updateNetworkNode(networkNodeData));
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertEquals(baseNodeNetworkService.getSingleNodeData( NodeTypeService.getNodeTypeList(false).get(0)), networkNodeData);
        NetworkNodeData newNetworkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        // Updated data sample
        String oldHttpPort = networkNodeData.getAddress();
        networkNodeData.setHttpPort(NEW_PORT);
        Assert.assertNotEquals(oldHttpPort, NEW_PORT);
        Assert.assertTrue(baseNodeNetworkService.updateNetworkNode(networkNodeData));
        NetworkNodeData updatedNetworkNodeData = baseNodeNetworkService.singleNodeNetworkDataMap.get(networkNodeData.getNodeType());
        Assert.assertEquals(NEW_PORT, updatedNetworkNodeData.getHttpPort());

        // For Multiple Node Type
        isMultipleNodeType = true;
        networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        Assert.assertFalse(baseNodeNetworkService.updateNetworkNode(networkNodeData));
        baseNodeNetworkService.addNode(networkNodeData);
        Assert.assertEquals(baseNodeNetworkService.multipleNodeMaps.get(NodeTypeService.getNodeTypeList(true).get(0)).get(networkNodeData.getHash()), networkNodeData);
        // Updated data sample
        String oldAddress = networkNodeData.getAddress();
        networkNodeData.setAddress(NEW_ADDRESS);
        Assert.assertNotEquals(oldAddress, "NEW_ADDRESS");

        Assert.assertTrue(baseNodeNetworkService.updateNetworkNode(networkNodeData));
        updatedNetworkNodeData = baseNodeNetworkService.getMapFromFactory(NodeTypeService.getNodeTypeList(true).get(0)).get(networkNodeData.getHash());
        Assert.assertEquals(NEW_ADDRESS, updatedNetworkNodeData.getAddress());
    }

    @Test(expected = Exception.class)
    public void validateNetworkNodeData_invalidNetworkType_throwException() throws Exception {
        //INVALID_NETWORK_TYPE
        boolean isMultipleNodeType = true;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        networkNodeData.setNetworkType(NetworkType.AlphaNet);
        baseNodeNetworkService.validateNetworkNodeData(networkNodeData);
    }

    @Test(expected = Exception.class)
    public void validateNetworkNodeData_invalidSignature_throwException() throws Exception {
        //INVALID_SIGNATURE
        boolean isMultipleNodeType = true;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        baseNodeNetworkService.validateNetworkNodeData(networkNodeData);
    }

    @Test(expected = Exception.class)
    public void validateNetworkNodeData_invalidNodeRegistrationSignature_throwException() throws Exception {
        //INVALID_NODE_REGISTRATION_SIGNATURE
        boolean isMultipleNodeType = true;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        when(networkNodeCrypto.verifySignature(networkNodeData)).thenReturn(true);
        baseNodeNetworkService.validateNetworkNodeData(networkNodeData);
    }


    @Test(expected = Exception.class)
    public void validateNetworkNodeData_invalidNodeRegistrar_throwException() throws Exception {
        //INVALID_NODE_REGISTRAR
        boolean isMultipleNodeType = true;
        NetworkNodeData networkNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        NodeRegistrationData nodeRegistrationData = new NodeRegistrationData();
        nodeRegistrationData.setRegistrarHash(generateRandomHash());
        networkNodeData.setNodeRegistrationData(nodeRegistrationData);
        when(networkNodeCrypto.verifySignature(networkNodeData)).thenReturn(true);
        when(nodeRegistrationCrypto.verifySignature(networkNodeData.getNodeRegistrationData())).thenReturn(true);
        baseNodeNetworkService.validateNetworkNodeData(networkNodeData);
    }

    @Test
    public void isNodeExistsOnMemory() {
        // For Multiple Node Type
        boolean isMultipleNodeType = true;
        NetworkNodeData networNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        boolean nodeExistsOnMemory = baseNodeNetworkService.isNodeExistsOnMemory(networNodeData);
        Assert.assertFalse(nodeExistsOnMemory); // Before adding node
        baseNodeNetworkService.addNode(networNodeData);
        nodeExistsOnMemory = baseNodeNetworkService.isNodeExistsOnMemory(networNodeData);
        Assert.assertTrue(nodeExistsOnMemory);  // After adding node

        // For Single Node Type
        isMultipleNodeType = false;
        networNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
        nodeExistsOnMemory = baseNodeNetworkService.isNodeExistsOnMemory(networNodeData);
        Assert.assertFalse(nodeExistsOnMemory); // Before adding node
        baseNodeNetworkService.addNode(networNodeData);
        nodeExistsOnMemory = baseNodeNetworkService.isNodeExistsOnMemory(networNodeData);
        Assert.assertTrue(nodeExistsOnMemory);  // After adding node
    }

    @Test
    public void getShuffledNetworkNodeDataListFromMapValues() {
        // For Multiple Node Type
        NodeTypeService.getNodeTypeList(true).forEach(nodeType -> {
            Assert.assertNotNull(baseNodeNetworkService.getShuffledNetworkNodeDataListFromMapValues(nodeType));
        });

        // For Single Node Type
        NodeTypeService.getNodeTypeList(false).forEach(nodeType -> {
            Assert.assertNull(baseNodeNetworkService.getShuffledNetworkNodeDataListFromMapValues(nodeType));
        });
    }

    @Test
    public void getNetworkNodeDataList() {
        List<NetworkNodeData> networkNodeDataList = baseNodeNetworkService.getNetworkNodeDataList();
        Assert.assertTrue(networkNodeDataList.size()==0);

        // For Multiple Node Type
        boolean isMultipleNodeType = true;
        int multipleNodeTypesAmount = NodeTypeService.getNodeTypeList(isMultipleNodeType).size();
        NodeTypeService.getNodeTypeList(isMultipleNodeType).forEach(nodeType -> {

            NetworkNodeData networNodeData = generateRandomNetworkNodeData(isMultipleNodeType);
            networNodeData.setNodeType(nodeType);
            baseNodeNetworkService.addNode(networNodeData);
        });
        networkNodeDataList = baseNodeNetworkService.getNetworkNodeDataList();
        Assert.assertEquals(multipleNodeTypesAmount, networkNodeDataList.size() );

        // For Single Node Type
        boolean isSingleNodeType = false;
        int singleNodeTypesAmount = NodeTypeService.getNodeTypeList(isSingleNodeType).size();
        NodeTypeService.getNodeTypeList(isSingleNodeType).forEach(nodeType -> {

            NetworkNodeData networNodeData = generateRandomNetworkNodeData(isSingleNodeType);
            networNodeData.setNodeType(nodeType);
            baseNodeNetworkService.addNode(networNodeData);
        });
        networkNodeDataList = baseNodeNetworkService.getNetworkNodeDataList();
        Assert.assertEquals(multipleNodeTypesAmount+singleNodeTypesAmount, networkNodeDataList.size());
    }

}
