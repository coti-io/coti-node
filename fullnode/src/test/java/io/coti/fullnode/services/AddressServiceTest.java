package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.Addresses;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AddressService.class,
        Addresses.class,
        BaseNodeRocksDBConnector.class}
)
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        AddressServiceTest.class})
@Slf4j
public class AddressServiceTest extends AbstractTestExecutionListener {

    @Autowired
    private AddressService addressService;

    @Autowired
    private IDatabaseConnector rocksDBConnector;

    @MockBean
    private WebSocketSender webSocketSender;

    @MockBean
    private ISender sender;

    @Override
    public void beforeTestClass(TestContext testContext) {
        BaseNodeRocksDBConnector rocksDBConnector =
                testContext.getApplicationContext().getBean("rocksDBConnector", BaseNodeRocksDBConnector.class);
        rocksDBConnector.init();
    }

    @Test
    public void addNewAddress() {
        boolean isAddressNewInDb = addressService.addNewAddress(generateRandomHash());
        Assert.assertTrue(isAddressNewInDb);
    }

    @Test
    public void handlePropagatedAddress_noExceptionIsThrown() {
        try {
            addressService.handlePropagatedAddress(new AddressData(generateRandomHash()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
