package unitTest;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.basenode.services.ClusterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseNodeAddressService.class,
        Addresses.class,
        RocksDBConnector.class}
        )
@Slf4j
public class BaseNodeAddressServiceTest {
    private static boolean setUpIsDone = false;

    @Autowired
    private BaseNodeAddressService baseNodeAddressService;

    @Autowired
    private IDatabaseConnector rocksDBConnector;
    @Before
    public void init() {
        if (setUpIsDone) {
            return;
        }

        try {
            rocksDBConnector.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setUpIsDone = true;
    }
    @Test
    public void testAddNewAddress() {
        boolean isAddressNewInDb = baseNodeAddressService.addNewAddress(generateRandomHash(64));
        Assert.assertTrue(isAddressNewInDb);
    }
    @Test

    public void testAddressExists() {
        Assert.assertFalse(baseNodeAddressService.addressExists(generateRandomHash(64)));
    }

    @Test
    public void handlePropagatedAddress_noExceptionIsThrown() {
        try {
            baseNodeAddressService.handlePropagatedAddress(new AddressData(generateRandomHash(64)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
