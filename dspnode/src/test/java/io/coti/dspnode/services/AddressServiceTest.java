package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {AddressService.class
,ValidationService.class
})
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @MockBean
    private InitializationService initializationService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private Addresses addresses;

    @MockBean
    private IPropagationPublisher propagationPublisher;

    @MockBean
    private ValidationService validationService;


    @Test
    public void handleNewAddressFromFullNode() {
        AddressData addressData = TestUtils.generateRandomAddressData();
        // To complete OK
        when(validationService.validateAddress(addressData.getHash())).thenReturn(true);
        addressService.handleNewAddressFromFullNode(addressData);
        Mockito.verify(propagationPublisher, Mockito.times(1)).propagate(any(AddressData.class), anyListOf(NodeType.class));

        // When Address exists
        when(addresses.getByHash(any(Hash.class))).thenReturn(addressData);
        addressService.handleNewAddressFromFullNode(addressData);
        Mockito.verify(propagationPublisher, Mockito.times(1)).propagate(any(AddressData.class), anyListOf(NodeType.class));
    }

}
