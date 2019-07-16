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
        // read all addresses from rocksdb
        // send all addresses to storage
        // Storage: store addresses
        // receive response from storage node.
        //delete addresses that were successfully stored.
        List<AddressData> addressesData = new ArrayList<>();
        addresses.forEach(addressesData::add);

        //TODO 7/15/2019 astolia: sign
        //TODO 7/15/2019 astolia: should be chunked.
//        getAddressesFromStorage(addressesData);
//        AddAddressesBulkRequest addAddressesBulkRequest = new AddAddressesBulkRequest(addressesData);
//        ResponseEntity<AddAddressesBulkResponse> response = storageConnector.storeInStorage("TODO",addAddressesBulkRequest, AddAddressesBulkResponse.class);
    }

    public ResponseEntity<GetHistoryAddressesResponse> getAddresses(GetHistoryAddressesRequest getHistoryAddressesRequest) {
//        if(!addressCrypto.verifyGetAddressRequestSignatureMessage(getHistoryAddressesRequest)) {
        if(!addressesRequestCrypto.verifySignature(getHistoryAddressesRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetHistoryAddressesResponse(new LinkedHashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        List<Hash> addressesHashes = getHistoryAddressesRequest.getAddressesHash();
        Map<Hash,AddressData> addressToAddressDataResponse = populateAndRemoveFoundAddresses(addressesHashes);
        ResponseEntity<GetHistoryAddressesResponse> storageResponse = getAddressesFromStorage(addressesHashes);
        GetHistoryAddressesResponse getHistoryAddressesResponse =  storageResponse.getBody();

        //TODO 7/16/2019 astolia: check signed by storage
        if(!validationService.validateGetAddressesResponse(getHistoryAddressesResponse, NodeType.StorageNode)){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetHistoryAddressesResponse(new LinkedHashMap<>(),"Response validation failed", BaseNodeHttpStringConstants.STATUS_ERROR));
        }
        //TODO 7/15/2019 astolia: check if fields are null to avoid nullpointer
        if(!addressesResponseCrypto.verifySignature(getHistoryAddressesResponse)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetHistoryAddressesResponse(new LinkedHashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        addressToAddressDataResponse.putAll((storageResponse.getBody()).getAddressHashesToAddresses());
        return ResponseEntity.status(HttpStatus.OK).body(new GetHistoryAddressesResponse(addressToAddressDataResponse, "Addresses successfully retrieved", BaseNodeHttpStringConstants.STATUS_SUCCESS));
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
