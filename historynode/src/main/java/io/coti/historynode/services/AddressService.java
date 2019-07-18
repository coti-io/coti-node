package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.AddressesRequestCrypto;
import io.coti.basenode.crypto.AddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.basenode.services.BaseNodeValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;


@Slf4j
@Service
public class AddressService extends BaseNodeAddressService{//extends EntityService {
    @Autowired
    protected StorageConnector storageConnector;
    @Autowired
    private Addresses addresses;
    @Autowired
    private BaseNodeValidationService validationService;
    @Autowired
    private AddressesResponseCrypto addressesResponseCrypto;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;
    @Value("${storage.server.address}")
    private String storageServerAddress;

    private ObjectMapper mapper;

    @PostConstruct
    private void initiate() {
        mapper = new ObjectMapper();
    }

    protected void continueHandleGeneratedAddress(AddressData addressData) {

    }

    public void handleClusterStsmpAddressesStorage(){
        List<AddressData> addressesData = new ArrayList<>();
        addresses.forEach(addressesData::add);
        // send all addresses to storage
        // Storage: store addresses
        // receive response from storage node.
        //delete addresses that were successfully stored.

        //TODO 7/15/2019 astolia: sign
        //TODO 7/15/2019 astolia: should be chunked.
//        getAddressesFromStorage(addressesData);
//        AddHistoryAddressesRequest addAddressesBulkRequest = new AddHistoryAddressesRequest(addressesData);
//        ResponseEntity<AddHistoryAddressesResponse> response = storageConnector.storeInStorage("TODO",addAddressesBulkRequest, AddHistoryAddressesResponse.class);
    }

    public ResponseEntity<GetHistoryAddressesResponse> getAddresses(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        if(getHistoryAddressesRequest.getSignature() == null || getHistoryAddressesRequest.getSignerHash() == null || !addressesRequestCrypto.verifySignature(getHistoryAddressesRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetHistoryAddressesResponse(new LinkedHashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        List<Hash> addressesHashesToGetFromStorage = getHistoryAddressesRequest.getAddressesHash();
        Map<Hash,AddressData> addressToAddressDataFromDB = populateAndRemoveFoundAddresses(addressesHashesToGetFromStorage);
        ResponseEntity<GetHistoryAddressesResponse> storageResponse = getAddressesFromStorage(addressesHashesToGetFromStorage);

        GetHistoryAddressesResponse getHistoryAddressesResponse =  storageResponse.getBody();

        //TODO 7/16/2019 astolia: check signed by storage
        if(!validationService.validateGetAddressesResponse(getHistoryAddressesResponse, NodeType.StorageNode)){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetHistoryAddressesResponse(new LinkedHashMap<>(),"Response validation failed", BaseNodeHttpStringConstants.STATUS_ERROR));
        }
        //TODO 7/15/2019 astolia: check if fields are null to avoid nullpointer
        if(!addressesResponseCrypto.verifySignature(getHistoryAddressesResponse)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetHistoryAddressesResponse(new LinkedHashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        //addressToAddressDataResponse.putAll(reorderHashResponses(getHistoryAddressesRequest.getAddressesHash(), storageResponse.getBody().getAddressHashesToAddresses()));
        return ResponseEntity.
                status(HttpStatus.OK).
                body(new GetHistoryAddressesResponse(
                        reorderHashResponses(getHistoryAddressesRequest.getAddressesHash(),
                                addressToAddressDataFromDB,
                                storageResponse.getBody().getAddressHashesToAddresses()),
                        "Addresses successfully retrieved",
                        BaseNodeHttpStringConstants.STATUS_SUCCESS));
    }

    private Map<Hash, AddressData> reorderHashResponses(List<Hash> originallyOrderedAddressHashes, Map<Hash, AddressData> addressHashesToAddressesFromDB, Map<Hash, AddressData> addressHashesToAddressesFromStorage) {
        Map<Hash, AddressData> orderedResponse = new LinkedHashMap<>();
        originallyOrderedAddressHashes.stream().forEach( hash -> {
            AddressData addressData = addressHashesToAddressesFromDB.get(hash);
            if(addressData == null){
                addressData = addressHashesToAddressesFromStorage.get(hash);
            }
            orderedResponse.put(hash,addressData);
        });
        return orderedResponse;
    }

    private Map<Hash,AddressData> populateAndRemoveFoundAddresses(List<Hash> addressesHashes){
        Map<Hash,AddressData> addressesFoundInDb = new HashMap<>();

        addressesHashes.forEach(addressHash -> {
            AddressData addressData = addresses.getByHash(addressHash);
            if(addressData != null){
                addressesFoundInDb.put(addressHash,addressData);
            }
        });
        addressesFoundInDb.keySet().forEach(addressesHashes::remove);
        return addressesFoundInDb;
    }

    private ResponseEntity<GetHistoryAddressesResponse> getAddressesFromStorage(List<Hash> addressesHashes) {
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressesHashes);
        addressesRequestCrypto.signMessage(getHistoryAddressesRequest);
        return  storageConnector.retrieveFromStorage(storageServerAddress + "/addresses", getHistoryAddressesRequest, GetHistoryAddressesResponse.class);
    }
}
