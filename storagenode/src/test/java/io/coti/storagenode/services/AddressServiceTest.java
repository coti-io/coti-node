package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.http.GetObjectBulkJsonResponse;
import io.coti.storagenode.http.GetObjectJsonResponse;
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
import static io.coti.storagenode.http.HttpStringConstants.STATUS_OK;
import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {AddressService.class, DbConnectorService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressServiceTest {

    private static final int NUMBER_OF_ADDRESSES = 4;
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
        AddressData addressData1 = new AddressData(generateRandomHash());
        AddressData addressData2 = new AddressData(generateRandomHash());

        String addressAsJson = mapper.writeValueAsString(addressData1);
        addressService.insertObjectJson(addressData1.getHash(), addressAsJson, true);
        addressService.insertObjectJson(addressData2.getHash(), addressAsJson, true);

        IResponse deleteResponse = addressService.deleteObjectByHash(addressData2.getHash(), true).getBody();

        IResponse getResponse = addressService.getObjectByHash(addressData1.getHash(), true).getBody();
        Assert.assertTrue(((BaseResponse) (getResponse)).getStatus().equals(STATUS_SUCCESS) &&
                ((GetObjectJsonResponse) deleteResponse).status.equals(STATUS_SUCCESS));
    }

    @Test
    public void multiAddressTest() throws IOException {
        Map<Hash, String> hashToAddressJsonDataMap = new HashMap<>();
        List<AddressData> AddressDataList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressData addressData = new AddressData(generateRandomHash());
            AddressDataList.add(addressData);
            hashToAddressJsonDataMap.put(addressData.getHash(), mapper.writeValueAsString(addressData));
        }
        addressService.insertMultiObjects(hashToAddressJsonDataMap, true);

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(AddressDataList.get(0).getHash());
        deleteHashes.add(AddressDataList.get(1).getHash());

        IResponse deleteResponse = addressService.deleteMultiObjectsFromDb(deleteHashes, true).getBody();

        List<Hash> GetHashes = new ArrayList<>();
        GetHashes.add(AddressDataList.get(2).getHash());
        GetHashes.add(AddressDataList.get(3).getHash());

        IResponse response = addressService.getMultiObjectsFromDb(GetHashes, true).getBody();

        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS)
                && ((GetObjectBulkJsonResponse) deleteResponse).getHashToObjectsFromDbMap().get(AddressDataList.get(0).getHash()).equals(STATUS_OK)
                && ((GetObjectBulkJsonResponse) deleteResponse).getHashToObjectsFromDbMap().get(AddressDataList.get(1).getHash()).equals(STATUS_OK));
    }
}