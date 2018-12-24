package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.config.WebShutDown;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.model.Addresses;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class AddressServiceTest {

    // private static boolean setUpIsDone = false;

    @Autowired
    private AddressService addressService;

    @MockBean
    private InitializationService initializationService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private IDatabaseConnector rocksDBConnector;

    @MockBean
    private WebShutDown webShutDown;
    //
    @MockBean
    private Addresses addresses;

    @MockBean
    private IPropagationPublisher propagationPublisher;

    @Before
    public void init() {
//        if (setUpIsDone) {
//            return;
//        }
//
//        try {
//            rocksDBConnector.init();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        setUpIsDone = true;
    }

    @Test
    public void handleNewAddressFromFullNode() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        addressService.handleNewAddressFromFullNode(new AddressData(generateRandomHash(64)));
    }

}
