package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.*;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.fullnode.controllers.AddressController;
import io.coti.fullnode.services.AddressService;
import javafx.beans.binding.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static javafx.beans.binding.Bindings.when;
import static org.junit.Assert.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {AddressService.class, AddressController.class, Addresses.class})
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceTest {


    @Autowired
    private AddressService addressService;

    @MockBean
    private WebSocketSender mockWebSocketSender;

    @MockBean
    private AddressController mockAddressController;

    @MockBean
    private Addresses mockAddresses;

    @MockBean
    private ISender mockISender;

    @MockBean
    private INetworkService mockINetworkService;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void continueHandleGeneratedAddress()
    {
        Hash hash = TestUtils.generateRandomHash();
        AddressData addressData = new AddressData( hash );
        addressService.continueHandleGeneratedAddress( addressData );
        log.info("Address {} is about to be sent to the subscribed user", addressData.getHash().toHexString());
    }

    @Test
    public void addAddress() {
        List<NetworkNodeData> networkNodeDataList = new ArrayList<NetworkNodeData>();
        NetworkNodeData networkNodeData = TestUtils.generateRandomNetworkNodeData();
        networkNodeDataList.add(networkNodeData);
        Mockito.when(mockINetworkService.getShuffledNetworkNodeDataListFromMapValues(NodeType.DspNode)).thenReturn(networkNodeDataList);

        Hash hash = TestUtils.generateRandomHash();
        boolean addAddress = addressService.addAddress(hash);
        Assert.assertTrue(addAddress);
    }

    @Test
    public void stamTest() { int b= 7;}
}