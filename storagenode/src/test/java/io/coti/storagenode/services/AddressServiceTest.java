package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {AddressService.class, ClientService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private ClientService clientService;

    @Test
    public void addressTest() throws IOException {
        AddressData addressData = new AddressData(generateRandomHash());
        ObjectMapper mapper = new ObjectMapper();
        String transactionAsJson = mapper.writeValueAsString(new AddressData(generateRandomHash()));
        addressService.insertAddressJson(addressData.getHash(), transactionAsJson);
        addressService.insertAddressJson(addressData.getHash(), transactionAsJson);
        IResponse response = addressService.getAddressByHash(addressData.getHash()).getBody();
        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS));
    }
}