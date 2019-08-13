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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Data
@Service
@Slf4j
public class AddressStorageService extends EntityStorageService {

    @Autowired
    private JacksonSerializer jacksonSerializer;
    @Autowired
    private BaseNodeValidationService validationService;
    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;

    @PostConstruct
    public void init() {
        super.objectType = ElasticSearchData.ADDRESSES;
    }

    public ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        try {
            if (!getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SerializableResponse(INVALID_SIGNATURE, STATUS_ERROR));
            }
            Map<Hash, AddressData> hashToAddressDataMap = new HashMap<>();
            super.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest.getAddressHashes()).forEach((hash, addressString) ->
                    hashToAddressDataMap.put(hash, addressString != null ? jacksonSerializer.deserialize(addressString) : null));
            GetHistoryAddressesResponse getHistoryAddressesResponse = new GetHistoryAddressesResponse(hashToAddressDataMap);
            getHistoryAddressesResponseCrypto.signMessage(getHistoryAddressesResponse);
            return ResponseEntity.ok(getHistoryAddressesResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(e.getMessage(), STATUS_ERROR));
        }
    }

    public boolean validateObjectDataIntegrity(Hash addressHash, String addressAsJson) {
        AddressData addressData = jacksonSerializer.deserialize(addressAsJson);
        if (addressData == null) {
            return false;
        }
        return validationService.validateAddress(addressData.getHash());
    }

    public Map<Hash, AddressData> getObjectsMapFromJsonMap(HashMap<Hash, String> responsesMap) {
        Map<Hash, AddressData> hashAddressDataMap = responsesMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e ->
                jacksonSerializer.deserialize(e.getValue())
        ));

        return hashAddressDataMap;
    }

    @Override
    protected GetHistoryAddressesResponse getEmptyEntitiesBulkResponse() {
        return new GetHistoryAddressesResponse();
    }
}
