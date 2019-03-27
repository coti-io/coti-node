package io.coti.zerospend.services;


import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.interfaces.ICommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {NetworkService.class, BaseNodeNetworkService.class})
@RunWith(SpringRunner.class)
@Slf4j
public class NetworkServiceTest {

    @Autowired
    private NetworkService networkService;

    @MockBean
    private ICommunicationService communicationService;
    @MockBean
    private NetworkNodeCrypto networkNodeCrypto;
    @MockBean
    private NodeRegistrationCrypto nodeRegistrationCrypto;

    @Test
    public void handleNetworkChanges() {
        networkService.init();
        NetworkData networkData = TestUtils.generateRandomNetworkData();
        networkService.handleNetworkChanges(networkData);
    }

}
