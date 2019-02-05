package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {AddressService.class, DbConnectorService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private DbConnectorService dbConnectorService;

    private ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper();
    }

    @Test
    public void addressTest() throws IOException {
        AddressData addressData = new AddressData(generateRandomHash());
        String addressAsJson = mapper.writeValueAsString(addressData);
        addressService.insertObjectJson(addressData.getHash(), addressAsJson);
        addressService.insertObjectJson(addressData.getHash(), addressAsJson);
        IResponse response = addressService.getObjectByHash(addressData.getHash()).getBody();
        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS));
    }

    @Test
    public void multiAddressTest() throws IOException {
        AddressData addressData1 = new AddressData(generateRandomHash());
        AddressData addressData2 = new AddressData(generateRandomHash());

        Map<Hash, String> hashToAddressJsonDataMap = new HashMap<>();
        hashToAddressJsonDataMap.put(addressData1.getHash(), mapper.writeValueAsString(addressData1));
        hashToAddressJsonDataMap.put(addressData2.getHash(), mapper.writeValueAsString(addressData2));
        addressService.insertMultiObjects(hashToAddressJsonDataMap);

        List<Hash> hashes = new ArrayList<>();
        hashes.add(addressData1.getHash());
        hashes.add(addressData2.getHash());

        IResponse response = addressService.getMultiObjectsFromDb(hashes).getBody();
        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS));
    }
}