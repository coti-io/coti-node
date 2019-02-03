package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.http.AddObjectJsonResponse;
import io.coti.storagenode.http.GetObjectBulkJsonResponse;
import io.coti.storagenode.http.GetObjectJsonResponse;
import io.coti.storagenode.services.interfaces.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
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
    public ResponseEntity<IResponse> insertMultiAddresses(Map<Hash, String> hashToAddressJsonDataMap) {

        try {
            clientService.insertMultiObjectsToDb(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME, hashToAddressJsonDataMap);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        //TODO: Define logic
        return null;

        //clientService.insertMultiObjectsToDb(addressDocumentList);
    }

    @Override
    public ResponseEntity<IResponse> insertAddressJson(Hash hash, String addressAsJson) {
        String insertResponse = null;
        try {
            insertResponse = clientService.insertObjectToDb(hash, addressAsJson, ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddObjectJsonResponse(
                        STATUS_SUCCESS,
                        ADDRESS_CREATED_MESSAGE, insertResponse));
    }

    @Override
    public ResponseEntity<IResponse> getMultiAddressesFromDb(List<Hash> hashes) {
        Map<Hash, String> hashToAddressFromDbMap = null;
        //TODO: Define logic.
        try {
            hashToAddressFromDbMap = clientService.getMultiObjects(hashes, ADDRESS_INDEX_NAME);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetObjectBulkJsonResponse(hashToAddressFromDbMap));
    }

    @Override
    public ResponseEntity<IResponse> getAddressByHash(Hash hash) {
        String addressAsJson = null;
        try {
            addressAsJson = clientService.getObjectFromDbByHash(hash, ADDRESS_INDEX_NAME);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            ADDRESS_TRANSACTIONS_SERVER_ERROR,
                            STATUS_ERROR));
        }
        if (addressAsJson == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            ADDRESS_DOESNT_EXIST_MESSAGE,
                            STATUS_ERROR));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetObjectJsonResponse(hash, addressAsJson));
    }

    @Override
    public ResponseEntity<IResponse> deleteMultiAddressesFromDb(List<Hash> hashes) {
        //TODO: Define logic.
        try {
            for (Hash hash : hashes) {
                clientService.deleteObject(hash, ADDRESS_INDEX_NAME);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public ResponseEntity<IResponse> deleteAddressByHash(Hash hash) {
        clientService.deleteObject(hash, ADDRESS_INDEX_NAME);
        return null;
    }
}
