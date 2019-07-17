package io.coti.historynode.services;


import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import utils.AddressTestUtils;
import utils.TestConstants;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration Test scenarios:
 * 1) Store + Retrieve 2 Addresses
 * 2) Store + Retrieve 2 Transactions
 *
 * TODO q/edge cases:
 * what is desired result when storing an already stored address/transaction?
 * trying to retrieve non existing address/transaction
 */
@ContextConfiguration(classes = {StorageConnector.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageConnectorIntegrationTest {

    @Autowired
    private  StorageConnector storageConnector;

//    private static ObjectMapper mapper;

    @BeforeClass
    public static void setupOnce(){
//        mapper = new ObjectMapper()
//                .registerModule(new ParameterNamesModule())
//                .registerModule(new Jdk8Module())
//                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    //TODO 7/14/2019 astolia: rename as convection
    @Test
    public void storeAndRetrieveAddresses(){
        int size = 2;

        // Store in elastic and check correct size and correct result values.
        List<AddressData> addresses = AddressTestUtils.generateListOfRandomAddressData(size);
        AddAddressesBulkRequest req = new AddAddressesBulkRequest(addresses);
//        ResponseEntity<AddAddressesBulkResponse> storeResponse = storageConnector.storeInStorage(TestConstants.storeMultipleAddressToStorageUrl, req, AddAddressesBulkResponse.class);
        ResponseEntity<EntitiesBulkJsonResponse> storeResponse = storageConnector.storeInStorage(TestConstants.storeMultipleAddressToStorageUrl, req, EntitiesBulkJsonResponse.class);

//        Assert.assertEquals(storeResponse.getBody().getAddressHashesToStoreResult().size(),size);
        Assert.assertEquals(storeResponse.getBody().getHashToEntitiesFromDbMap().size(),size);
//        storeResponse.getBody().getAddressHashesToStoreResult().values().forEach(storeResult -> Assert.assertEquals(true,storeResult));
        //TODO 7/16/2019 tomer: should be matched with UPDATED \ CREATED
//        storeResponse.getBody().getHashToEntitiesFromDbMap().values().forEach(storeResult -> Assert.assertEquals(true,storeResult));

        // get from elastic addresses that were stored above. make sure correct Http status, correct size, correct address data.
//        ResponseEntity<GetAddressesBulkResponse> retrieveResponse = storageConnector.retrieveFromStorage(TestConstants.getAddressesFromStorageUrl, new GetAddressesBulkRequest(addresses.stream().map(addressData -> addressData.getHash()).collect(Collectors.toList())),GetAddressesBulkResponse.class);
        ResponseEntity<EntitiesBulkJsonResponse> retrieveResponse = storageConnector.retrieveFromStorage(TestConstants.getAddressesFromStorageUrl, new GetAddressesBulkRequest(addresses.stream().map(addressData -> addressData.getHash()).collect(Collectors.toList())),EntitiesBulkJsonResponse.class);
        Assert.assertEquals(retrieveResponse.getStatusCode(),HttpStatus.OK);
//        Map<Hash, AddressData> addressHashesToAddresses = retrieveResponse.getBody().getHashToEntitiesFromDbMap();
////        Map<Hash, AddressData> addressHashesToAddresses = retrieveResponse.getBody().getAddressHashesToAddresses();
//        Assert.assertEquals(addressHashesToAddresses.size(),size);
//        Set<Hash> hashes = addresses.stream().map(addressData -> addressData.getHash()).collect(Collectors.toSet());
//        addressHashesToAddresses.values().forEach(addressData -> Assert.assertTrue(hashes.contains(addressData.getHash())));

        int iPause =7;

    }


//    @Test
//    public void storeAndRetrieveTransactions(){
//        int size = 2;
//
//        Set<TransactionData> transactions = TransactionTestUtils.generateSetOfRandomTransactionData(size);
//        Map<Hash,String> transactionAsJsonString = new HashMap<>();
//        transactions.forEach( t -> transactionAsJsonString.put(t.getHash(),mapTransactionToJsonString(t)));
//        AddEntitiesBulkRequest request = new AddEntitiesBulkRequest(transactionAsJsonString);
//    }

//    private String mapTransactionToJsonString(TransactionData t){
//        try {
//            return mapper.writeValueAsString(t);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
}
