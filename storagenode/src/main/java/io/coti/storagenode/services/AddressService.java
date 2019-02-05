package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.MultiDbInsertionStatus;
import io.coti.storagenode.http.AddObjectJsonResponse;
import io.coti.storagenode.http.GetObjectBulkJsonResponse;
import io.coti.storagenode.http.GetObjectJsonResponse;
import io.coti.storagenode.services.interfaces.IAddressService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.storagenode.http.HttpStringConstants.STATUS_NOT_FOUND;
import static io.coti.storagenode.http.HttpStringConstants.STATUS_OK;

@Slf4j
@Service
public class AddressService implements IAddressService {
    @Autowired
    private DbConnectorService dbConnectorService;

    private String ADDRESS_INDEX_NAME = "address";
    private String ADDRESS_OBJECT_NAME = "addressData";


    @PostConstruct
    private void init() throws Exception {
        try {
            dbConnectorService.addIndex(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<IResponse> insertMultiAddresses(Map<Hash, String> hashToAddressJsonDataMap) {
        Pair<MultiDbInsertionStatus, Map<Hash, String>> insertResponse = null;
        try {
            insertResponse = dbConnectorService.insertMultiObjectsToDb(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME, hashToAddressJsonDataMap);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        return ResponseEntity
                .status(dbConnectorService.getHttpStatus(insertResponse.getKey()))
                .body(new GetObjectBulkJsonResponse(insertResponse.getValue()));
    }

    @Override
    public ResponseEntity<IResponse> insertAddressJson(Hash hash, String addressAsJson) {
        String insertResponse = null;
        try {
            insertResponse = dbConnectorService.insertObjectToDb(hash, addressAsJson, ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new AddObjectJsonResponse(
                        STATUS_SUCCESS,
                        CREATED_MESSAGE, insertResponse));
    }

    @Override
    public ResponseEntity<IResponse> getMultiAddressesFromDb(List<Hash> hashes) {
        Map<Hash, String> hashToAddressFromDbMap = null;
        //TODO: Define logic.
        try {
            hashToAddressFromDbMap = dbConnectorService.getMultiObjects(hashes, ADDRESS_INDEX_NAME);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetObjectBulkJsonResponse(hashToAddressFromDbMap));
    }

    @Override
    public ResponseEntity<IResponse> getAddressByHash(Hash hash) {
        String addressAsJson = null;
        try {
            addressAsJson = dbConnectorService.getObjectFromDbByHash(hash, ADDRESS_INDEX_NAME);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            e.getMessage(),
                            STATUS_ERROR));
        }
        if (addressAsJson == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            SERVER_ERROR,
                            STATUS_ERROR));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetObjectJsonResponse(hash, addressAsJson));
    }

    @Override
    public ResponseEntity<IResponse> deleteMultiAddressesFromDb(List<Hash> hashes) {
        Map<Hash, String> hashToResponseMap = new HashMap<>();
        try {
            for (Hash hash : hashes) {
                hashToResponseMap.put(hash, dbConnectorService.deleteObject(hash, ADDRESS_INDEX_NAME));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            SERVER_ERROR,
                            STATUS_ERROR));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetObjectBulkJsonResponse(hashToResponseMap));
    }

    @Override
    public ResponseEntity<IResponse> deleteAddressByHash(Hash hash) {
        String status = dbConnectorService.deleteObject(hash, ADDRESS_INDEX_NAME);
        switch (status) {
            case STATUS_OK:
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new GetObjectJsonResponse(hash, status));
            case STATUS_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new GetObjectJsonResponse(hash, status));
            default:
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(
                                SERVER_ERROR,
                                STATUS_ERROR));

        }
    }
}
