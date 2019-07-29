package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.DbConnectorService;
import org.elasticsearch.rest.RestStatus;
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

import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {ObjectService.class, DbConnectorService.class, JacksonSerializer.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressServiceTest {

    private static final int NUMBER_OF_ADDRESSES = 4;
    @Autowired
    private ObjectService addressService;

    @Autowired
    private DbConnectorService dbConnectorService;

    @Autowired
    protected JacksonSerializer jacksonSerializer;

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
        RestStatus insertRestStatus = addressService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(insertRestStatus.equals(RestStatus.CREATED));
        String returnedAddressAsJson = addressService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(addressAsJson.equals(returnedAddressAsJson));
    }


    @Test
    public void addressTest() throws IOException {
        AddressData addressData1 = new AddressData(generateRandomHash());
        AddressData addressData2 = new AddressData(generateRandomHash());

        String addressAsJson = mapper.writeValueAsString(addressData1);
//        Assert.assertTrue(addressData1.equals(jacksonSerializer.deserialize(addressAsJson))); //TODO 7/29/2019 tomer: mismatched with mapper

        RestStatus insertRestStatus1 = addressService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(insertRestStatus1.equals(RestStatus.CREATED));
        RestStatus insertRestStatus2 = addressService.insertObjectJson(addressData2.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(insertRestStatus2.equals(RestStatus.CREATED));

        RestStatus deleteRestStatus = addressService.deleteObjectByHash(addressData2.getHash(), true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(deleteRestStatus.equals(RestStatus.OK));

        String returnedAddressAsJson = addressService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(addressData1.equals(mapper.readValue(returnedAddressAsJson, AddressData.class)));
//        Assert.assertTrue(addressData1.equals(jacksonSerializer.deserialize(addressAsJson))); //TODO 7/29/2019 tomer: mismatched with mapper
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
        Map<Hash, RestStatus> hashToRestStatusInsertResponseMap = addressService.insertMultiObjects(hashToAddressJsonDataMap, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(hashToRestStatusInsertResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.CREATED)));

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(AddressDataList.get(0).getHash());
        deleteHashes.add(AddressDataList.get(1).getHash());

        Map<Hash, RestStatus> hashToRestStatusDeleteResponseMap = addressService.deleteMultiObjectsFromDb(deleteHashes, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(hashToRestStatusDeleteResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.OK)));

        List<Hash> getHashes = new ArrayList<>();
        getHashes.add(AddressDataList.get(2).getHash());
        getHashes.add(AddressDataList.get(3).getHash());

        Map<Hash, String> hashToRestStatusGetResponseMap = addressService.getMultiObjectsFromDb(getHashes, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(hashToRestStatusGetResponseMap.size() == getHashes.size());
        Assert.assertTrue(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(0))).equals(AddressDataList.get(2)));
        Assert.assertTrue(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(1))).equals(AddressDataList.get(3)));
    }


}