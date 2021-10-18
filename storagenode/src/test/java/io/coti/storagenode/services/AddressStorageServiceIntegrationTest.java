//package io.coti.storagenode.services;
//
//import io.coti.basenode.communication.JacksonSerializer;
//import io.coti.basenode.crypto.*;
//import io.coti.basenode.data.AddressData;
//import io.coti.basenode.data.Hash;
//import io.coti.basenode.database.interfaces.IDatabaseConnector;
//import io.coti.basenode.model.Transactions;
//import io.coti.basenode.services.BaseNodeValidationService;
//import io.coti.basenode.services.interfaces.IPotService;
//import io.coti.basenode.services.interfaces.ITransactionHelper;
//import io.coti.storagenode.data.enums.ElasticSearchData;
//import io.coti.storagenode.database.DbConnectorService;
//import org.elasticsearch.rest.RestStatus;
//import org.junit.Assert;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//import testUtils.HashTestUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@ContextConfiguration(classes = {AddressStorageService.class,
//        ObjectService.class, JacksonSerializer.class, GetHistoryAddressesRequestCrypto.class,
//        GetHistoryAddressesResponseCrypto.class, NodeCryptoHelper.class, DbConnectorService.class})
//@TestPropertySource(locations = "classpath:test.properties")
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class AddressStorageServiceIntegrationTest {
//
//    private final int NUMBER_OF_ADDRESSES = 4;
//
//    @Autowired
//    private AddressStorageService addressStorageService;
//    @Autowired
//    protected JacksonSerializer jacksonSerializer;
//    @Autowired
//    private ObjectService objectService;
//    @Autowired
//    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
//    @Autowired
//    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
//
//    @MockBean
//    private BaseNodeValidationService validationService;
//    @Autowired
//    protected DbConnectorService dbConnectorService;
//    @MockBean
//    public IDatabaseConnector databaseConnector;
//
//    @MockBean
//    private Transactions transactions;
//    @MockBean
//    private ITransactionHelper transactionHelper;
//    @MockBean
//    private TransactionCrypto transactionCrypto;
//    @MockBean
//    private TransactionSenderCrypto transactionSenderCrypto;
//    @MockBean
//    private IPotService potService;
//    @Autowired
//    private NodeCryptoHelper nodeCryptoHelper;
//
//
//    // @Test
//    public void insertAndGetAddressTest() {
//        AddressData addressData1 = new AddressData(HashTestUtils.generateRandomAddressHash());
//        String addressAsJson = jacksonSerializer.serializeAsString(addressData1);
//        RestStatus insertRestStatus = objectService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(insertRestStatus.equals(RestStatus.CREATED));
//        String returnedAddressAsJson = objectService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(addressAsJson.equals(returnedAddressAsJson));
//    }
//
//
//    //@Test
//    public void addressTest() {
//        AddressData addressData1 = new AddressData(HashTestUtils.generateRandomAddressHash());
//        AddressData addressData2 = new AddressData(HashTestUtils.generateRandomAddressHash());
//
//        String addressAsJson = jacksonSerializer.serializeAsString(addressData1);
//        Assert.assertTrue(addressData1.equals(jacksonSerializer.deserialize(addressAsJson)));
//
//        RestStatus insertRestStatus1 = objectService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(insertRestStatus1.equals(RestStatus.CREATED));
//        RestStatus insertRestStatus2 = objectService.insertObjectJson(addressData2.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(insertRestStatus2.equals(RestStatus.CREATED));
//
//        RestStatus deleteRestStatus = objectService.deleteObjectByHash(addressData2.getHash(), true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(deleteRestStatus.equals(RestStatus.OK));
//
//        String returnedAddressAsJson = objectService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(addressData1.equals(jacksonSerializer.deserialize(returnedAddressAsJson)));
//    }
//
//    //@Test
//    public void multiAddressTest() {
//        List<AddressData> AddressDataList = new ArrayList<>();
//        Map<Hash, String> hashToAddressJsonDataMap = new HashMap<>();
//
//        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
//            AddressData addressData = new AddressData(HashTestUtils.generateRandomAddressHash());
//            AddressDataList.add(addressData);
//            hashToAddressJsonDataMap.put(addressData.getHash(), jacksonSerializer.serializeAsString(addressData));
//        }
//        Map<Hash, RestStatus> hashToRestStatusInsertResponseMap = objectService.insertMultiObjects(hashToAddressJsonDataMap, true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(hashToRestStatusInsertResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.CREATED)));
//
//        List<Hash> deleteHashes = new ArrayList<>();
//        deleteHashes.add(AddressDataList.get(0).getHash());
//        deleteHashes.add(AddressDataList.get(1).getHash());
//
//        Map<Hash, RestStatus> hashToRestStatusDeleteResponseMap = objectService.deleteMultiObjectsFromDb(deleteHashes, true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(hashToRestStatusDeleteResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.OK)));
//
//        List<Hash> getHashes = new ArrayList<>();
//        getHashes.add(AddressDataList.get(2).getHash());
//        getHashes.add(AddressDataList.get(3).getHash());
//
//        Map<Hash, String> hashToRestStatusGetResponseMap = objectService.getMultiObjectsFromDb(getHashes, true, ElasticSearchData.ADDRESSES);
//        Assert.assertTrue(hashToRestStatusGetResponseMap.size() == getHashes.size());
//        Assert.assertNotNull(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(0))));
//        Assert.assertTrue(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(0))).equals(AddressDataList.get(2)));
//        Assert.assertTrue(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(1))).equals(AddressDataList.get(3)));
//    }
//}
