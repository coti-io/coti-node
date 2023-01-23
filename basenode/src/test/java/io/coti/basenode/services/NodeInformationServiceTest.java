package io.coti.basenode.services;

import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.basenode.http.NodeInformationResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {NodeInformationService.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(OutputCaptureExtension.class)
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

    @Test
    void getNodeInformation_exception(CapturedOutput output) {
        try (MockedStatic<InetAddress> utilities = Mockito.mockStatic(InetAddress.class)) {
            utilities.when(InetAddress::getLocalHost).thenThrow(UnknownHostException.class);
            nodeInformationService.getNodeInformation();
            assertTrue(output.getOut().contains("Unknown host at node information"));
        }
    }
}