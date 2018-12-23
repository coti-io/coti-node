package unitTest;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
import io.coti.dspnode.services.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

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
    private ISender sender;

    @MockBean
    private IPropagationPublisher propagationPublisher;

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
    public void handleNewAddressFromFullNode() {
        addressService.handleNewAddressFromFullNode(new AddressData(generateRandomHash(64)));
    }

}
