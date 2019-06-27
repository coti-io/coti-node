package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetEntitiesBulkResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.http.GetEntitiesBulkJsonResponse;
import io.coti.storagenode.http.GetEntityJsonResponse;
import io.coti.storagenode.services.interfaces.IEntityStorageService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public abstract class EntityStorageService implements IEntityStorageService
{

    @Autowired
    private HistoryNodesConsensusService historyNodesConsensusService;

    @Override
    public ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectAsJsonString)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateStoreObjectToStorage(hash, objectAsJsonString);
        if( !isResponseOK(response) )
            return response;

        // Store data in ongoing storage system
        response = getObjectService().insertObjectJson(hash, objectAsJsonString, false);
        if( !isResponseOK(response) )
            return response; // TODO consider some retry mechanism,
        else {
            // Store data also in cold-storage
            response = getObjectService().insertObjectJson(hash, objectAsJsonString, true);
            if ( !isResponseOK(response) )
                return response; // TODO consider some retry mechanism,
        }
        return response;
    }


    @Override
    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, String fieldName)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateRetrieveObjectToStorage(hash);
        if( !isResponseOK(response) )
            return response;

        // Retrieve data from ongoing storage system, including data integrity checks
        boolean fromColdStorage = false;
        ResponseEntity<IResponse> objectByHashResponse = getObjectService().getObjectByHash(hash, fromColdStorage, fieldName);

        if( !isResponseOK(objectByHashResponse) )
        {
            fromColdStorage = true;
            objectByHashResponse = getObjectService().getObjectByHash(hash, fromColdStorage, fieldName);
            if( !isResponseOK(objectByHashResponse) )
                return objectByHashResponse;   // Failed to retrieve from both repositories
        }


        String objectAsJson = ((GetEntityJsonResponse)objectByHashResponse.getBody()).getEntityJsonPair().getValue();
        Hash objectHash = ((GetEntityJsonResponse)objectByHashResponse.getBody()).getEntityJsonPair().getKey();
//        Hash objectHash = ((Pair<Hash, String>) objectByHashResponse.getBody()).getKey();
//        String objectAsJson = ((Pair<Hash, String>) objectByHashResponse.getBody()).getValue();

        return verifyRetrievedSingleObject(objectHash, objectAsJson, fromColdStorage, fieldName);
    }


    private ResponseEntity<IResponse> verifyRetrievedSingleObject(Hash objectHash, String objectAsJson, boolean fromColdStorage, String fieldName)
    {
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(objectAsJson);

        // If DI is successful for ongoing repository, remove redundant data from cold-storage
        if( isObjectDIOK(objectHash, objectAsJson) )
        {
            if( !fromColdStorage )
            {
                // If DI is successful, try to delete potential duplicate data from cold-storage
                ResponseEntity<IResponse> deleteResponse =  getObjectService().deleteObjectByHash(objectHash, true);
                if( !isResponseOK(deleteResponse) )
                    return response; // Delete can fail due to previous deletion, should not impact flow
            }
            return response;
        }

        // If DI failed, retrieve data from cold-storage, remove previous data from ongoing storage system,
        ResponseEntity<IResponse> coldStorageResponse = getObjectService().getObjectByHash(objectHash, true, fieldName);
        if( isResponseOK(coldStorageResponse) )
        {
            Hash coldObjectHash = ((Pair<Hash, String>) coldStorageResponse.getBody()).getKey();
            String coldObjectAsJson = ((Pair<Hash, String>) coldStorageResponse.getBody()).getValue();
            // Check DI from cold storage, should never fail
            if( !isObjectDIOK(coldObjectHash, coldObjectAsJson) )
            {
                ResponseEntity failedResponse = ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body( "Failed to retrieve = "+ objectHash.toString()+ " from all repositories. \n");
                return failedResponse;
            }
            // Delete compromised copy from ongoing repository
            ResponseEntity<IResponse> deleteResponse =  getObjectService().deleteObjectByHash(objectHash, false);
            if ( isResponseOK(deleteResponse) )
            {   // Copy data from cold-storage to hot-storage
                ResponseEntity<IResponse> insertResponse = getObjectService().insertObjectJson(coldObjectHash, coldObjectAsJson, false);
                // TODO consider verifying response for possible retry mechanism
            }
        }
        return coldStorageResponse;
    }


    private ResponseEntity<IResponse> validateRetrieveObjectToStorage(Hash hash)
    {
        List<Hash> hashToObjectJsonStringList = new ArrayList<>();
        hashToObjectJsonStringList.add(hash);

        //TODO: implement or remove method
        ResponseEntity<IResponse> responsesEntity = new ResponseEntity(HttpStatus.OK);
        return responsesEntity;
    }


    @Override
    public ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap);
        if( !isResponseOK(response) )
            return response;

        // Store data from approved request into ongoing storage
        response = getObjectService().insertMultiObjects(hashToObjectJsonDataMap, false);
        if( !isResponseOK(response) )
            return response; // TODO consider some retry mechanism
        else
        {
            response = getObjectService().insertMultiObjects(hashToObjectJsonDataMap, true);
            if( !isResponseOK(response) )
                return response; // TODO consider some retry mechanism, consider removing from ongoing storage
        }
        return response;
    }

    private ResponseEntity<IResponse> validateStoreObjectToStorage(Hash hash, String objectJson)
    {
        Map<Hash, String> hashToObjectJsonDataMap = new HashMap<>();
        hashToObjectJsonDataMap.put(hash, objectJson);
        // Validate consensus decision on provided data from history nodes' master + Data Integrity
        ResponseEntity<IResponse> response = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap);
        return response;
    }


    private ResponseEntity<IResponse> validateStoreMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap)
    {
        // Validate consensus decision on provided Transactions data from history nodes' master
        ResponseEntity<IResponse> response = new ResponseEntity(HttpStatus.OK);

        // Additional Validations after consensus checks - Data integrity
        if( !hashToObjectJsonDataMap.entrySet().stream().allMatch( entry -> isObjectDIOK(entry.getKey(), entry.getValue()) ) )
        {
            response = new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        return response;
    }


    @Override
    public ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(List<Hash> hashes, String fieldName)
    {
//        HashMap<Hash, ResponseEntity<IResponse>> responsesMap = new HashMap<>();
        HashMap<Hash, String> responsesMap = new HashMap<>();
        GetEntitiesBulkResponse entitiesBulkResponse = new GetEntitiesBulkResponse(responsesMap);

        // Retrieve data from ongoing storage system
        ResponseEntity<IResponse> objectsByHashResponse = getObjectService().getMultiObjectsFromDb(hashes, false, fieldName);

        if( !isResponseOK(objectsByHashResponse) )
        {
            responsesMap.put(null, null);
            entitiesBulkResponse.setEntitiesBulkResponses(responsesMap);
            return ResponseEntity.status(objectsByHashResponse.getStatusCode()).body(entitiesBulkResponse);
        }

        // For successfully retrieved data, perform also data-integrity checks
        ((GetEntitiesBulkJsonResponse)objectsByHashResponse.getBody()).getHashToEntitiesFromDbMap().forEach( (hash, objectAsJsonString) ->
                {
                    //TODO: In case of error in retrieving valid value, return value as a null.
                    ResponseEntity<IResponse> verifyRetrievedSingleObject = verifyRetrievedSingleObject(hash, objectAsJsonString, false, fieldName);
                    if (verifyRetrievedSingleObject.getStatusCode() != HttpStatus.OK) {
                        responsesMap.put(hash, null);
                    } else {
                        responsesMap.put(hash, objectAsJsonString);
                    }
                }
        );
        return ResponseEntity.status(HttpStatus.OK).body(entitiesBulkResponse);
    }


    protected boolean isResponseOK(ResponseEntity<IResponse> iResponse) {
        return iResponse != null && iResponse.getStatusCode().equals(HttpStatus.OK);
    }


}
