package io.coti.storagenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.DbConnectorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import testUtils.AddressTestUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.storagenode.services.NodeServiceManager.dbConnectorService;
import static io.coti.storagenode.services.NodeServiceManager.objectService;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressStorageService.class,
        JacksonSerializer.class, BaseNodeValidationService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith({SpringExtension.class})
class AddressStorageServiceTest {

    @Autowired
    protected JacksonSerializer jacksonSerializerLocal;
    AddressData addressDataInStorage1 = AddressTestUtils.generateRandomAddressData();
    AddressData addressDataInStorage2 = AddressTestUtils.generateRandomAddressData();
    AddressData addressDataNotInStorage1 = AddressTestUtils.generateRandomAddressData();
    AddressData addressDataNotInStorage2 = AddressTestUtils.generateRandomAddressData();
    @Autowired
    private AddressStorageService addressStorageService;
    @Autowired
    private BaseNodeValidationService validationServiceLocal;
    @MockBean
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCryptoLocal;
    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCryptoLocal;
    @MockBean
    private ObjectService objectServiceLocal;
    @MockBean
    private DbConnectorService dbConnectorServiceLocal;

    @BeforeEach
    void init() {
        getHistoryAddressesResponseCrypto = getHistoryAddressesResponseCryptoLocal;
        getHistoryAddressesRequestCrypto = getHistoryAddressesRequestCryptoLocal;
        jacksonSerializer = jacksonSerializerLocal;
        objectService = objectServiceLocal;
        dbConnectorService = dbConnectorServiceLocal;
        validationService = validationServiceLocal;
    }

    /**
     * Test request received with bad signature.
     */
    @Test
    void retrieveMultipleObjectsFromStorage_badSignature_errorReturned() {
        GetHistoryAddressesRequest getHistoryAddressesRequest = AddressTestUtils.generateGetHistoryAddressesRequest(
                null, null,
                addressDataInStorage1);

        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(false);

        ResponseEntity<IResponse> actualResponse = addressStorageService.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest);
        SerializableResponse expectedResponse = new SerializableResponse(INVALID_SIGNATURE, STATUS_ERROR);

        Assertions.assertEquals(expectedResponse, actualResponse.getBody());
    }

    /**
     * retrieve 2 address from storage.
     * both address are found in elastic and will be returned.
     */
    @Test
    void retrieveMultipleObjectsFromStorage_addressesInStorage_returnsAddresses() {
        GetHistoryAddressesRequest getHistoryAddressesRequest = AddressTestUtils.generateGetHistoryAddressesRequest(
                null, null,
                addressDataInStorage1,
                addressDataInStorage2);
        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(true);

        mockGetMultiObjectsFromDb(ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.TRUE, Boolean.TRUE),
                addressDataInStorage1,
                addressDataInStorage2);

        mockGetMultiObjectsFromDb(ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.TRUE, Boolean.TRUE),
                addressDataInStorage1,
                addressDataInStorage2);

        ResponseEntity<IResponse> actualResponse = addressStorageService.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest);
        ResponseEntity<IResponse> expectedResponse = generateExpectedResponse(generateInStorageList(Boolean.TRUE, Boolean.TRUE),
                addressDataInStorage1, addressDataInStorage2);
        checkResponseEquality(expectedResponse, actualResponse);
    }

    /**
     * retrieve 2 address from storage.
     * both address are not found in elastic and null will be returned.
     */
    @Test
    void retrieveMultipleObjectsFromStorage_addressesNotInStorage_returnsNulls() {
        GetHistoryAddressesRequest getHistoryAddressesRequest = AddressTestUtils.generateGetHistoryAddressesRequest(
                null, null,
                addressDataNotInStorage1,
                addressDataNotInStorage2);
        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(true);

        mockGetMultiObjectsFromDb(ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.FALSE, Boolean.FALSE),
                addressDataNotInStorage1,
                addressDataNotInStorage2);

        mockGetMultiObjectsFromDb(ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.FALSE, Boolean.FALSE),
                addressDataNotInStorage1,
                addressDataNotInStorage2);

        ResponseEntity<IResponse> actualResponse = addressStorageService.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest);
        ResponseEntity<IResponse> expectedResponse = generateExpectedResponse(generateInStorageList(Boolean.FALSE, Boolean.FALSE),
                addressDataNotInStorage1, addressDataNotInStorage2);
        checkResponseEquality(expectedResponse, actualResponse);
    }

    private ResponseEntity<IResponse> generateExpectedResponse(List<Boolean> inStorage, AddressData... addresses) {
        Map<Hash, AddressData> hashToAddressDataMap = new HashMap<>();
        AtomicInteger index = new AtomicInteger(0);
        Arrays.stream(addresses).forEach(addressData -> hashToAddressDataMap.put(addressData.getHash(), inStorage.get(index.getAndIncrement()) == Boolean.TRUE ? addressData : null));
        GetHistoryAddressesResponse getHistoryAddressesResponse = new GetHistoryAddressesResponse(hashToAddressDataMap);
        getHistoryAddressesResponseCrypto.signMessage(getHistoryAddressesResponse);

        return ResponseEntity.ok(getHistoryAddressesResponse);
    }

    private void checkResponseEquality(ResponseEntity<IResponse> expectedResponse, ResponseEntity<IResponse> actualResponse) {
        Assertions.assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());

        Map<Hash, AddressData> expectedAddressHashesToAddresses = ((GetHistoryAddressesResponse) expectedResponse.getBody()).getAddressHashesToAddresses();
        Map<Hash, AddressData> actualAddressHashesToAddresses = ((GetHistoryAddressesResponse) actualResponse.getBody()).getAddressHashesToAddresses();
        Assertions.assertEquals(expectedAddressHashesToAddresses.size(), actualAddressHashesToAddresses.size());

        Iterator iterExpected = expectedAddressHashesToAddresses.entrySet().iterator();
        Iterator iterActual = actualAddressHashesToAddresses.entrySet().iterator();
        while (iterExpected.hasNext()) {
            Map.Entry<Hash, AddressData> expectedEntry = (Map.Entry<Hash, AddressData>) iterExpected.next();
            Map.Entry<Hash, AddressData> actualEntry = (Map.Entry<Hash, AddressData>) iterActual.next();

            Assertions.assertEquals(expectedEntry.getValue(), actualEntry.getValue());
            Assertions.assertEquals(expectedEntry.getKey(), actualEntry.getKey());
        }
    }

    private List<Boolean> generateInStorageList(Boolean... inStorageList) {
        List<Boolean> inStorageListReturn = new ArrayList<>();
        inStorageListReturn.addAll(Arrays.asList(inStorageList));
        return inStorageListReturn;

    }

    private void mockGetMultiObjectsFromDb(ElasticSearchData objectType, List<Boolean> inStorage, AddressData... addresses) {
        List<Hash> hashes = new ArrayList<>();
        Map<Hash, String> objectsFromDBMap = new HashMap<>();
        AtomicInteger index = new AtomicInteger(0);
        Arrays.stream(addresses).forEach(addressData -> {

            hashes.add(addressData.getHash());
            objectsFromDBMap.put(addressData.getHash(), inStorage.get(index.getAndIncrement()) == Boolean.TRUE ? jacksonSerializer.serializeAsString(addressData) : null);
        });
        when(objectService.getMultiObjectsFromDb(hashes, false, objectType)).thenReturn(objectsFromDBMap);
    }

}