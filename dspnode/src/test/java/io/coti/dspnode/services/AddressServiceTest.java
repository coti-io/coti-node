package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.model.Addresses;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {AddressService.class})
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


    @Test
    public void handleNewAddressFromFullNode() {
        AddressData addressData = TestUtils.generateRandomAddressData();
        String handleNewAddressFromFullNodeResponse = addressService.handleNewAddressFromFullNode(addressData);
        //TODO:  method handleNewAddressFromFullNode returns a simple String as response. Consider either changing to Response with Body & Status or turn into void as in TransactionService.handleNewTransactionFromFullNode
        Assert.assertTrue(handleNewAddressFromFullNodeResponse.equals("OK"));

        when(addresses.getByHash(any(Hash.class))).thenReturn(addressData);
        handleNewAddressFromFullNodeResponse = addressService.handleNewAddressFromFullNode(addressData);
        Assert.assertTrue(handleNewAddressFromFullNodeResponse.equals("Address exists"));
    }

}
