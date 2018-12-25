package io.coti.basenode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeAddressService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static testUtils.TestUtils.generateRandomHash;

//@TestPropertySource(locations = "classpath:test.properties")
//@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseNodeAddressService.class,
        Addresses.class,
        RocksDBConnector.class}
)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeAddressServiceTest {
    private static final int SIZE_OF_HASH = 64;
    private static boolean setUpIsDone = false;

    @Autowired
    private BaseNodeAddressService baseNodeAddressService;

    @Autowired
    private IDatabaseConnector rocksDBConnector;

    @Before
    public void setUp() {
        if (setUpIsDone) {
            return;
        }
        log.info("Starting  - " + this.getClass().getSimpleName());
        try {
            rocksDBConnector.init();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        setUpIsDone = true;
    }

    @Test
    public void testAddNewAddress() {
        boolean isAddressNewInDb = baseNodeAddressService.addNewAddress(generateRandomHash(SIZE_OF_HASH ));
        Assert.assertTrue(isAddressNewInDb);
    }

    @Test
    public void testAddressExists() {
        Assert.assertFalse(baseNodeAddressService.addressExists(generateRandomHash(SIZE_OF_HASH )));
    }

    @Test
    public void handlePropagatedAddress_noExceptionIsThrown() {
        try {
            baseNodeAddressService.handlePropagatedAddress(new AddressData(generateRandomHash(SIZE_OF_HASH )));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {

        }
    }
}
