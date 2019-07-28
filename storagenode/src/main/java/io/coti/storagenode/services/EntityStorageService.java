package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.http.EntitiesBulkJsonResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.http.GetEntityJsonResponse;
import io.coti.storagenode.services.interfaces.IEntityStorageService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class EntityStorageService implements IEntityStorageService {

    public static final String TRANSACTION_DATA = "transactionData";

    @Autowired
    protected ObjectService objectService;

    @Autowired
    private AddressStorageService addressStorageService;

    protected ElasticSearchData objectType;

    @Override
    public ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectAsJsonString) {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateStoreObjectToStorage(hash, objectAsJsonString);
        if (!isResponseOK(response))
            return response;

        // Store data in ongoing storage system
        response = objectService.insertObjectJson(hash, objectAsJsonString, false, objectType);
        if (!isResponseOK(response))
            return response; // TODO consider some retry mechanism,
        else {
            // Store data also in cold-storage
            response = objectService.insertObjectJson(hash, objectAsJsonString, true, objectType);
            if (!isResponseOK(response))
                return response; // TODO consider some retry mechanism,
        }
        return response;
    }


    @Override
    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, ElasticSearchData objectType) {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateRetrieveObjectToStorage(hash);
        if (!isResponseOK(response))
            return response;

        // Retrieve data from ongoing storage system, including data integrity checks
        boolean fromColdStorage = false;
        ResponseEntity<IResponse> objectByHashResponse = objectService.getObjectByHash(hash, fromColdStorage, objectType);

        if (!isResponseOK(objectByHashResponse)) {
            fromColdStorage = true;
            objectByHashResponse = objectService.getObjectByHash(hash, fromColdStorage, objectType);
            if (!isResponseOK(objectByHashResponse))
                return objectByHashResponse;   // Failed to retrieve from both repositories
        }


        String objectAsJson = ((GetEntityJsonResponse) objectByHashResponse.getBody()).getEntityJsonPair().getValue();
        Hash objectHash = ((GetEntityJsonResponse) objectByHashResponse.getBody()).getEntityJsonPair().getKey();
//        Hash objectHash = ((Pair<Hash, String>) objectByHashResponse.getBody()).getKey();
//        String objectAsJson = ((Pair<Hash, String>) objectByHashResponse.getBody()).getValue();

        return verifyRetrievedSingleObject(objectHash, objectAsJson, fromColdStorage, objectType);
    }


    protected boolean verifyRetrievedSingleObject(Hash objectHash, String objectAsJson, boolean fromColdStorage, ElasticSearchData objectType) {

        if (objectAsJson != null && validateObjectDataIntegrity(objectHash, objectAsJson)) {
            if (!fromColdStorage) {
                try {
                    objectService.deleteObjectByHash(objectHash, true, objectType);
                } catch (Exception e) {
                    log.error("{}: {}", e.getClass().getName(), e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    private String replaceHotStorageObjectWithColdStorageObject(Hash objectHash, ElasticSearchData objectType) {
        try {
            String coldStorageObjectAsJson = objectService.getObjectByHash(objectHash, true, objectType);
            if (coldStorageObjectAsJson != null) {
                if (!validateObjectDataIntegrity(objectHash, coldStorageObjectAsJson)) {
                    return null;
                }
                RestStatus deleteStatus = objectService.deleteObjectByHash(objectHash, false, objectType);
                if (deleteStatus.equals(RestStatus.OK)) {
                    RestStatus insertStatus = objectService.insertObjectJson(objectHash, coldStorageObjectAsJson, false, objectType);
                    if (insertStatus.equals(RestStatus.OK)) {
                        return coldStorageObjectAsJson;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return null;
        }
    }


    private ResponseEntity<IResponse> validateRetrieveObjectToStorage(Hash hash) {
        List<Hash> hashToObjectJsonStringList = new ArrayList<>();
        hashToObjectJsonStringList.add(hash);

        //TODO: implement or remove method
        ResponseEntity<IResponse> responsesEntity = new ResponseEntity(HttpStatus.OK);
        return responsesEntity;
    }


    @Override
    public ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap) {
        Map<Hash, Boolean> entityFailedVerificationHashToFalse = new HashMap<>();
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap, entityFailedVerificationHashToFalse);
        if (!isResponseOK(response)) {
            return convertResponseStatusToBooleanAndAddFailedHashes(response, entityFailedVerificationHashToFalse);
        }

        // Store data from approved request into ongoing storage
        response = objectService.insertMultiObjects(hashToObjectJsonDataMap, false, objectType);
        if (!isResponseOK(response)) {
            return convertResponseStatusToBooleanAndAddFailedHashes(response, entityFailedVerificationHashToFalse);
        } else {
            response = objectService.insertMultiObjects(hashToObjectJsonDataMap, true, objectType);
            if (!isResponseOK(response)) {
                // TODO consider some retry mechanism, consider removing from ongoing storage
                return convertResponseStatusToBooleanAndAddFailedHashes(response, entityFailedVerificationHashToFalse);
            }
        }
        return convertResponseStatusToBooleanAndAddFailedHashes(response, entityFailedVerificationHashToFalse);
    }

    private ResponseEntity<IResponse> validateStoreObjectToStorage(Hash hash, String objectJson) {
        Map<Hash, String> hashToObjectJsonDataMap = new HashMap<>();
        Map<Hash, Boolean> entityFailedVerificationHashToFalse = new HashMap<>();
        hashToObjectJsonDataMap.put(hash, objectJson);
        // Validate Data Integrity
        ResponseEntity<IResponse> response = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap, entityFailedVerificationHashToFalse);
        return response;
    }


    private ResponseEntity<IResponse> validateStoreMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap, Map<Hash, Boolean> entityFailedVerificationHashToFalse) {
        ResponseEntity<IResponse> response = new ResponseEntity(HttpStatus.OK);

        // Validations checks - Data integrity
        hashToObjectJsonDataMap.entrySet().stream().forEach(entry -> {
            if (!validateObjectDataIntegrity(entry.getKey(), entry.getValue())) {
                entityFailedVerificationHashToFalse.put(entry.getKey(), Boolean.FALSE);
            }
        });
        entityFailedVerificationHashToFalse.entrySet().stream().forEach(entry -> {
            hashToObjectJsonDataMap.remove(entry.getKey());
        });
        if (hashToObjectJsonDataMap.isEmpty()) {
            response = new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }

        return response;
    }


//    @Override
//    public <G extends BaseResponse> ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(List<Hash> hashes, G entitiesBulkResponse)
//    {
//        HashMap<Hash, String> responsesMap = new HashMap<>();
//
//        // Retrieve data from ongoing storage system
//        ResponseEntity<IResponse> objectsByHashResponse = objectService.getMultiObjectsFromDb(hashes, false, objectType);
//
//        if( !isResponseOK(objectsByHashResponse)){
//            responsesMap.put(null,null);
//            if(objectType.getObjectName().equals(TRANSACTION_DATA)) {
//                ((GetHistoryTransactionsResponse)entitiesBulkResponse).setEntitiesBulkResponses(responsesMap);
//            } else {
//                ((GetHistoryAddressesResponse)entitiesBulkResponse).setAddressHashesToAddresses(addressStorageService.getObjectsMapFromJsonMap(responsesMap));
//            }
//            return ResponseEntity.status(objectsByHashResponse.getStatusCode()).body(entitiesBulkResponse);
//        }
//
//        // For successfully retrieved data, perform also data-integrity checks
//        ((EntitiesBulkJsonResponse)objectsByHashResponse.getBody()).getHashToEntitiesFromDbMap().forEach( (hash, objectAsJsonString) ->
//                {
//                    //TODO: In case of error in retrieving valid value, return value as a null.
//                    ResponseEntity<IResponse> verifyRetrievedSingleObject = verifyRetrievedSingleObject(hash, objectAsJsonString, false, objectType);
//                    if (verifyRetrievedSingleObject.getStatusCode() != HttpStatus.OK) {
//                        responsesMap.put(hash, null);
//                    } else {
//                        responsesMap.put(hash, objectAsJsonString);
//                    }
//                }
//        );
//        if(objectType.getObjectName().equals(TRANSACTION_DATA)) {
//            ((GetHistoryTransactionsResponse)entitiesBulkResponse).setEntitiesBulkResponses(responsesMap);
//        } else {
//            ((GetHistoryAddressesResponse)entitiesBulkResponse).setAddressHashesToAddresses(addressStorageService.getObjectsMapFromJsonMap(responsesMap));
//        }
//        return ResponseEntity.status(HttpStatus.OK).body(entitiesBulkResponse);
//    }


    protected boolean isResponseOK(ResponseEntity<IResponse> iResponse) {
        return iResponse != null && iResponse.getStatusCode().equals(HttpStatus.OK);
//               && ((BaseResponse)iResponse.getBody()).getStatus().equals("Success");
    }

    @Override
    public Map<Hash, String> retrieveMultipleObjectsFromStorage(List<Hash> hashes) {
        Map<Hash, String> responsesMap = new HashMap<>();

        Map<Hash, String> objectsFromDBMap = objectService.getMultiObjectsFromDb(hashes, false, objectType);

        verifyEntitiesFromDbMap(responsesMap, objectsFromDBMap);
        return responsesMap;

    }

    protected void verifyEntitiesFromDbMap(Map<Hash, String> responsesMap, Map<Hash, String> objectsFromDBMap) {
        objectsFromDBMap.forEach((hash, objectAsJsonString) ->
                {
                    if (!verifyRetrievedSingleObject(hash, objectAsJsonString, false, objectType)) {
                        responsesMap.put(hash, replaceHotStorageObjectWithColdStorageObject(hash, objectType));
                    } else {
                        responsesMap.put(hash, objectAsJsonString);
                    }
                }
        );
    }

    protected ResponseEntity<IResponse> convertResponseStatusToBooleanAndAddFailedHashes(ResponseEntity<IResponse> entityResponse, Map<Hash, Boolean> entityFailedConversionHashToFalse) {
        Map<Hash, String> entityResponseBodyMap = ((EntitiesBulkJsonResponse) entityResponse.getBody()).getHashToEntitiesFromDbMap();
        Map<Hash, Boolean> newResponseBody = new HashMap<>();
        entityResponseBodyMap.entrySet().forEach(entry -> {
            newResponseBody.put(entry.getKey(), convertElasticWriteStatusToBoolean(entry.getValue()));
        });
        newResponseBody.putAll(entityFailedConversionHashToFalse);
        return ResponseEntity.status(entityResponse.getStatusCode()).body(new AddHistoryEntitiesResponse(newResponseBody));
    }

    private Boolean convertElasticWriteStatusToBoolean(String writeStatus) {
        return writeStatus.equals("CREATED") || writeStatus.equals("UPDATED") ? Boolean.TRUE : Boolean.FALSE;
    }

    protected abstract IResponse getEmptyEntitiesBulkResponse();

    protected abstract IResponse getEntitiesBulkResponse(Map<Hash, String> responsesMap);


}
