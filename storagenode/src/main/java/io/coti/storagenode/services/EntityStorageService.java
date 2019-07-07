package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetEntitiesBulkResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.http.GetEntitiesBulkJsonResponse;
import io.coti.storagenode.http.GetEntityJsonResponse;
import io.coti.storagenode.model.ObjectService;
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

@Slf4j
public abstract class EntityStorageService implements IEntityStorageService
{

    @Autowired
    private HistoryNodesConsensusService historyNodesConsensusService;

    @Autowired
    private ObjectService objectService;

    protected ElasticSearchData objectType;

    @Override
    public ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectAsJsonString)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateStoreObjectToStorage(hash, objectAsJsonString);
        if( !isResponseOK(response) )
            return response;

        // Store data in ongoing storage system
        response = objectService.insertObjectJson(hash, objectAsJsonString, false, objectType);
        if( !isResponseOK(response) )
            return response; // TODO consider some retry mechanism,
        else {
            // Store data also in cold-storage
            response = objectService.insertObjectJson(hash, objectAsJsonString, true, objectType);
            if ( !isResponseOK(response) )
                return response; // TODO consider some retry mechanism,
        }
        return response;
    }


    @Override
    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, ElasticSearchData objectType)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateRetrieveObjectToStorage(hash);
        if( !isResponseOK(response) )
            return response;

        // Retrieve data from ongoing storage system, including data integrity checks
        boolean fromColdStorage = false;
        ResponseEntity<IResponse> objectByHashResponse = objectService.getObjectByHash(hash, fromColdStorage, objectType);

        if( !isResponseOK(objectByHashResponse) )
        {
            fromColdStorage = true;
            objectByHashResponse = objectService.getObjectByHash(hash, fromColdStorage, objectType);
            if( !isResponseOK(objectByHashResponse) )
                return objectByHashResponse;   // Failed to retrieve from both repositories
        }


        String objectAsJson = ((GetEntityJsonResponse)objectByHashResponse.getBody()).getEntityJsonPair().getValue();
        Hash objectHash = ((GetEntityJsonResponse)objectByHashResponse.getBody()).getEntityJsonPair().getKey();
//        Hash objectHash = ((Pair<Hash, String>) objectByHashResponse.getBody()).getKey();
//        String objectAsJson = ((Pair<Hash, String>) objectByHashResponse.getBody()).getValue();

        return verifyRetrievedSingleObject(objectHash, objectAsJson, fromColdStorage, objectType);
    }


    private ResponseEntity<IResponse> verifyRetrievedSingleObject(Hash objectHash, String objectAsJson, boolean fromColdStorage, ElasticSearchData objectType)
    {
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(objectAsJson);

        // If DI is successful for ongoing repository, remove redundant data from cold-storage
        if( objectAsJson!=null && isObjectDIOK(objectHash, objectAsJson) )
        {
            if( !fromColdStorage )
            {
                // If DI is successful, try to delete potential duplicate data from cold-storage
                ResponseEntity<IResponse> deleteResponse =  objectService.deleteObjectByHash(objectHash, true, objectType);
                if( !isResponseOK(deleteResponse) )
                    return response; // Delete can fail due to previous deletion, should not impact flow
            }
            return response;
        }

        // If DI failed, retrieve data from cold-storage, remove previous data from ongoing storage system,
        ResponseEntity<IResponse> coldStorageResponse = objectService.getObjectByHash(objectHash, true, objectType);
        if( isResponseOK(coldStorageResponse) )
        {
            Hash coldObjectHash = ((Pair<Hash, String>) coldStorageResponse.getBody()).getKey();
            String coldObjectAsJson = ((Pair<Hash, String>) coldStorageResponse.getBody()).getValue();
            // Check DI from cold storage, should never fail
            if( objectAsJson!=null && !isObjectDIOK(coldObjectHash, coldObjectAsJson) )
            {
                ResponseEntity failedResponse = ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body( "Failed to retrieve = "+ objectHash.toString()+ " from all repositories. \n");
                return failedResponse;
            }
            // Delete compromised copy from ongoing repository
            ResponseEntity<IResponse> deleteResponse =  objectService.deleteObjectByHash(objectHash, false, objectType);
            if ( isResponseOK(deleteResponse) )
            {   // Copy data from cold-storage to hot-storage
                ResponseEntity<IResponse> insertResponse = objectService.insertObjectJson(coldObjectHash, coldObjectAsJson, false, objectType);
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
        response = objectService.insertMultiObjects(hashToObjectJsonDataMap, false, objectType);
        if( !isResponseOK(response) )
            return response; // TODO consider some retry mechanism
        else
        {
            response = objectService.insertMultiObjects(hashToObjectJsonDataMap, true, objectType);
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
//        if( !hashToObjectJsonDataMap.entrySet().stream().allMatch( entry -> isObjectDIOK(entry.getKey(), entry.getValue()) ) )
//        {
//            response = new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
//        }
        return response;
    }


    @Override
    public ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(List<Hash> hashes)
    {
//        HashMap<Hash, ResponseEntity<IResponse>> responsesMap = new HashMap<>();
        HashMap<Hash, String> responsesMap = new HashMap<>();
        GetEntitiesBulkResponse entitiesBulkResponse = new GetEntitiesBulkResponse(responsesMap);

        // Retrieve data from ongoing storage system
        ResponseEntity<IResponse> objectsByHashResponse = objectService.getMultiObjectsFromDb(hashes, false, objectType);

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
                    ResponseEntity<IResponse> verifyRetrievedSingleObject = verifyRetrievedSingleObject(hash, objectAsJsonString, false, objectType);
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
//               && ((BaseResponse)iResponse.getBody()).getStatus().equals("Success");
    }


}
