package io.coti.basenode.services;


import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
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

import java.util.EnumSet;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration
        (classes = {
                BaseNodeNetworkService.class,
        }
        )
@Slf4j
public class BaseNodeNetworkServiceTest {

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
        int iPause = 7; //
    }


    @Test(expected = IllegalArgumentException)
    public void setSingleNodeData_unsupportedMultipleNetworkNodeType_exceptionIsThrown() {
        NodeTypeService.getNodeTypeList(true).forEach(nodeType -> baseNodeNetworkService.set(nodeType));
        int iPause = 7; //

    }
}
