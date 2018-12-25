package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
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

import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AddressService.class,
        Addresses.class,
        RocksDBConnector.class}
)
@Slf4j
public class AddressServiceTest {
    private static final int SIZE_OF_HASH = 64;
    private static boolean setUpIsDone = false;

    @Autowired
    private AddressService addressService;

    @Autowired
    private IDatabaseConnector rocksDBConnector;

    @MockBean
    private WebSocketSender webSocketSender;

    @MockBean
    private ISender sender;

    @Before
    public void setUp() {
        if (setUpIsDone) {
            return;
        }
        try {
            setUpIsDone = true;
            rocksDBConnector.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddNewAddress() {
        boolean isAddressNewInDb = addressService.addNewAddress(generateRandomHash(SIZE_OF_HASH ));
        Assert.assertTrue(isAddressNewInDb);
    }

    @Test
    public void testHandlePropagatedAddress_noExceptionIsThrown(){
        try {
            addressService.handlePropagatedAddress(new AddressData(generateRandomHash(SIZE_OF_HASH )));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
