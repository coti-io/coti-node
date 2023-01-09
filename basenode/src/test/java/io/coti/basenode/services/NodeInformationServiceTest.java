package io.coti.basenode.services;

import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.basenode.http.NodeInformationResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {NodeInformationService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class NodeInformationServiceTest {

    @Autowired
    private NodeInformationService nodeInformationService;

    @Test
    void getNodeInformation() {
        NodeInformationResponse nodeInformationResponse = nodeInformationService.getNodeInformation();
        Assertions.assertEquals(BaseNodeHttpStringConstants.STATUS_SUCCESS, nodeInformationResponse.getStatus());
    }
}