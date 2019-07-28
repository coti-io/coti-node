package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.DbConnectorService;
import io.coti.storagenode.http.GetEntityJsonResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@ContextConfiguration(classes = {ObjectService.class, DbConnectorService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressServiceTest {

    private static final int NUMBER_OF_ADDRESSES = 4;
    @Autowired
    private ObjectService addressService;

    @Autowired
    private DbConnectorService dbConnectorService;

    private ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper();
        try {
//            addressService.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insertAndGetAddressTest() throws IOException {
        AddressData addressData1 = new AddressData(generateRandomHash());
        String addressAsJson = mapper.writeValueAsString(addressData1);
        ResponseEntity<IResponse> responseResponseEntity1 = addressService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(responseResponseEntity1.getStatusCode().equals(HttpStatus.OK));
        IResponse getResponse = addressService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES).getBody();
        Assert.assertTrue(((BaseResponse) getResponse).getStatus().equals(STATUS_SUCCESS));
        Assert.assertEquals(((GetEntityJsonResponse) getResponse).getEntityJsonPair().getValue(), addressAsJson);
    }


    @Test
    public void addressTest() throws IOException {
        AddressData addressData1 = new AddressData(generateRandomHash());
        AddressData addressData2 = new AddressData(generateRandomHash());

        String addressAsJson = mapper.writeValueAsString(addressData1);
        ResponseEntity<IResponse> responseResponseEntity1 = addressService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(responseResponseEntity1.getStatus().equals(STATUS_SUCCESS))
        ResponseEntity<IResponse> responseResponseEntity2 = addressService.insertObjectJson(addressData2.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);

        IResponse deleteResponse = addressService.deleteObjectByHash(addressData2.getHash(), true, ElasticSearchData.ADDRESSES).getBody();

        IResponse getResponse = addressService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES).getBody();
        Assert.assertTrue(((BaseResponse) getResponse).getStatus().equals(STATUS_SUCCESS) &&
                ((GetEntityJsonResponse) deleteResponse).status.equals(STATUS_SUCCESS));
        int iPause = 7;
    }

    @Test
    public void multiAddressTest() throws IOException {
        List<AddressData> AddressDataList = new ArrayList<>();
        Map<Hash, String> hashToAddressJsonDataMap = new HashMap<>();

        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressData addressData = new AddressData(generateRandomHash());
            AddressDataList.add(addressData);
            hashToAddressJsonDataMap.put(addressData.getHash(), mapper.writeValueAsString(addressData));
        }
        ResponseEntity<IResponse> insertResponseEntity = addressService.insertMultiObjects(hashToAddressJsonDataMap, true, ElasticSearchData.ADDRESSES);

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(AddressDataList.get(0).getHash());
        deleteHashes.add(AddressDataList.get(1).getHash());

        IResponse deleteResponse = addressService.deleteMultiObjectsFromDb(deleteHashes, true, ElasticSearchData.ADDRESSES).getBody();

        List<Hash> GetHashes = new ArrayList<>();
        GetHashes.add(AddressDataList.get(2).getHash());
        GetHashes.add(AddressDataList.get(3).getHash());

        IResponse response = addressService.getMultiObjectsFromDb(GetHashes, true, ElasticSearchData.ADDRESSES).getBody();
        Assert.assertTrue(((EntitiesBulkJsonResponse) response).getStatus().equals(STATUS_SUCCESS));

        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS)
                && ((EntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(AddressDataList.get(0).getHash()).equals(STATUS_OK)
                && ((EntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(AddressDataList.get(1).getHash()).equals(STATUS_OK));
    }


}