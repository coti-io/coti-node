package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.ObjectDocument;
import io.coti.storagenode.http.AddAddressJsonResponse;
import io.coti.storagenode.http.GetMultiObjectJsonResponse;
import io.coti.storagenode.http.GetObjectJsonResponse;
import io.coti.storagenode.services.interfaces.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class AddressService implements IAddressService {
    @Autowired
    private ClientService clientService;

    private String ADDRESS_INDEX_NAME = "address";
    private String ADDRESS_OBJECT_NAME = "addressData";

    @PostConstruct
    private void init() {
        try {
            clientService.addIndex(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    @Override
    public ResponseEntity<IResponse> insertMultiAddressesToDb(List<ObjectDocument> addressDocumentList) throws IOException {
        clientService.insertMultiObjectsToDb(addressDocumentList);
        //TODO: Define logic
        return null;
    }

    @Override
    public ResponseEntity<IResponse> insertAddressJson(Hash hash, String addressAsJson) throws IOException {
        if (!validateAddress(hash, addressAsJson)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            INVALID_PARAMETERS_MESSAGE,
                            STATUS_ERROR));
        }
        String insertResponse =
                clientService.insertObjectToDb(hash, addressAsJson, ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddAddressJsonResponse(
                        STATUS_SUCCESS,
                        ADDRESS_CREATED_MESSAGE, insertResponse));
    }

    @Override
    public ResponseEntity<IResponse> getMultiAddressesFromDb(Map<Hash, String> hashAndIndexNameMap) throws IOException {
        Map<Hash, String> hashToObjectsFromDbMap = null;
        MultiGetResponse multiGetResponse = clientService.getMultiObjectsFromDb(hashAndIndexNameMap);
        hashToObjectsFromDbMap = new HashMap<>();
        for (MultiGetItemResponse multiGetItemResponse : multiGetResponse.getResponses()) {
            hashToObjectsFromDbMap.put(new Hash(multiGetItemResponse.getId()),
                    new String(multiGetItemResponse.getResponse().getSourceAsBytes()));
        }
        //TODO: Define logic.
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetMultiObjectJsonResponse(hashToObjectsFromDbMap));
    }


    @Override
    public ResponseEntity<IResponse> getAddressByHash(Hash hash) throws IOException {
        String addressAsJson = clientService.getObjectFromDbByHash(hash, ADDRESS_INDEX_NAME);
        if (addressAsJson == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            ADDRESS_DOESNT_EXIST_MESSAGE,
                            STATUS_ERROR));
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GetObjectJsonResponse(hash, addressAsJson));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            ADDRESS_TRANSACTIONS_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    private boolean validateAddress(Hash hash, String addressAsJsonString) throws IOException {
        // TODO:
        return true;
    }
}
