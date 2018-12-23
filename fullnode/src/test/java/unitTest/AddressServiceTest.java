package unitTest;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
import io.coti.fullnode.services.AddressService;
import io.coti.fullnode.services.WebSocketSender;
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

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AddressService.class,
        Addresses.class,
        RocksDBConnector.class}
)
@Slf4j
public class AddressServiceTest {
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
    public void init() {
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
    public void addNewAddress() {
        boolean isAddressNewInDb = addressService.addNewAddress(generateRandomHash(64));
        Assert.assertTrue(isAddressNewInDb );
    }

}
