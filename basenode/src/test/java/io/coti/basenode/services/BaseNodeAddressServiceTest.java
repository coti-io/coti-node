package io.coti.basenode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.ValidationService;
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
import testUtils.BaseNodeTestUtils;

import static org.mockito.Mockito.when;
import static testUtils.BaseNodeTestUtils.generateRandomAddressData;

@ContextConfiguration(classes = {BaseNodeAddressService.class,
        Addresses.class,
        BaseNodeRocksDBConnector.class,
}
)
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        BaseNodeAddressServiceTest.class})
@Slf4j
public class BaseNodeAddressServiceTest extends AbstractTestExecutionListener {

    @Autowired
    private BaseNodeAddressService baseNodeAddressService;

    @Autowired
    private BaseNodeRocksDBConnector baseNodeRocksDBConnector;

    @MockBean
    private ValidationService validationService;

    @Override
    public void beforeTestClass(TestContext testContext) {
        BaseNodeRocksDBConnector baseNodeRocksDBConnector =
                testContext.getApplicationContext().getBean("baseNodeRocksDBConnector", BaseNodeRocksDBConnector.class);
        baseNodeRocksDBConnector.init();


    }

    @Test
    public void addNewAddress() {
//        Hash addressHash = generateRandomHash();
        AddressData addressData = generateRandomAddressData();
        Hash addressHash = addressData.getHash();
        Assert.assertFalse(baseNodeAddressService.addressExists(addressHash));

        boolean result = baseNodeAddressService.addNewAddress(addressData);
        Assert.assertTrue(result);
        Assert.assertTrue(baseNodeAddressService.addressExists(addressHash));
    }

    @Test
    public void addressExists() {
        AddressData addressData = generateRandomAddressData();
        Hash addressHash = addressData.getHash();
//        Hash addressHash = generateRandomHash();
        Assert.assertFalse(baseNodeAddressService.addressExists(addressHash));
        Assert.assertTrue(baseNodeAddressService.addNewAddress(addressData));
        Assert.assertTrue(baseNodeAddressService.addressExists(addressHash));
    }

    @Test
    public void handlePropagatedAddress() {
        try {
            baseNodeAddressService.init();
            AddressData addressData = BaseNodeTestUtils.generateRandomAddressData();
//            when(validationService.validateAddress(any(Hash.class))).thenReturn(true);
            // For a non pre-existing address
            Assert.assertFalse(baseNodeAddressService.addressExists(addressData.getHash()));
            when(baseNodeAddressService.validateAddress(addressData.getHash())).thenReturn(true);
            baseNodeAddressService.handlePropagatedAddress(addressData);
            Assert.assertTrue(baseNodeAddressService.addressExists(addressData.getHash()));

            // For a pre-existing address
            baseNodeAddressService.handlePropagatedAddress(addressData);
            Assert.assertTrue(baseNodeAddressService.addressExists(addressData.getHash()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
