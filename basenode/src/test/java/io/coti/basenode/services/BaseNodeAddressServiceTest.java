package io.coti.basenode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {BaseNodeAddressService.class,
        Addresses.class,
        RocksDBConnector.class}
)
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        BaseNodeAddressServiceTest.class})
@Slf4j
public class BaseNodeAddressServiceTest  extends AbstractTestExecutionListener {

    @Autowired
    private BaseNodeAddressService baseNodeAddressService;

    @Autowired
    private IDatabaseConnector rocksDBConnector;

    @Override
    public void beforeTestClass(TestContext testContext){
        RocksDBConnector rocksDBConnector =
                testContext.getApplicationContext().getBean("rocksDBConnector", RocksDBConnector.class);
        rocksDBConnector.init();
    }

    @Test
    public void addNewAddress() {
        boolean isAddressNewInDb = baseNodeAddressService.addNewAddress(generateRandomHash());
        Assert.assertTrue(isAddressNewInDb);
    }

    @Test
    public void addressExists() {
        Assert.assertFalse(baseNodeAddressService.addressExists(generateRandomHash()));
    }

    @Test
    public void handlePropagatedAddress_noExceptionIsThrown() {
        try {
            baseNodeAddressService.handlePropagatedAddress(new AddressData(generateRandomHash()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
