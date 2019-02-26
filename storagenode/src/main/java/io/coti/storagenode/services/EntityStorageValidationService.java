package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.interfaces.IEntityStorageValidationService;
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
public abstract class EntityStorageValidationService implements IEntityStorageValidationService
{
    @Autowired
    private ObjectService objectService;
    
    @Autowired
    private HistoryNodesConsensusService historyNodesConsensusService;

    @Override
    public ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectAsJsonString, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateStoreObjectToStorage(hash, objectAsJsonString, historyNodeConsensusResult);
        if( !isResponseOK(response) )
            return response;

        // Store data in ongoing storage system
        response = objectService.insertObjectJson(hash, objectAsJsonString, false);
        if( !isResponseOK(response) )
            return response; // TODO consider some retry mechanism,
        else {
            // Store data also in cold-storage
            response = objectService.insertObjectJson(hash, objectAsJsonString, true);
            if ( !isResponseOK(response) )
                return response; // TODO consider some retry mechanism,
        }
        return response;
    }


    @Override
    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateRetrieveObjectToStorage(hash, historyNodeConsensusResult);
        if( !isResponseOK(response) )
            return response;

        // Retrieve data from ongoing storage system, including data integrity checks
        boolean fromColdStorage = false;
        ResponseEntity<IResponse> objectByHashResponse = objectService.getObjectByHash(hash, fromColdStorage);

        if( !isResponseOK(objectByHashResponse) )
        {
            fromColdStorage = true;
            objectByHashResponse = objectService.getObjectByHash(hash, fromColdStorage);
            if( !isResponseOK(objectByHashResponse) )
                return objectByHashResponse;   // Failed to retrieve from both repositories
        }


        Hash objectHash = ((Pair<Hash, String>) objectByHashResponse.getBody()).getKey();
        String objectAsJson = ((Pair<Hash, String>) objectByHashResponse.getBody()).getValue();

        return verifyRetrievedSingleObject(objectHash, objectAsJson, fromColdStorage);
    }


    private ResponseEntity<IResponse> verifyRetrievedSingleObject(Hash objectHash, String objectAsJson, boolean fromColdStorage)
    {
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body( objectAsJson);

        // If DI is successful for ongoing repository, remove redundant data from cold-storage
        if( isObjectDIOK(objectHash, objectAsJson) )
        {
            if( !fromColdStorage )
            {
                // If DI is successful, try to delete potential duplicate data from cold-storage
                ResponseEntity<IResponse> deleteResponse =  objectService.deleteObjectByHash(objectHash, true);
                if( !isResponseOK(deleteResponse) )
                    return response; // Delete can fail due to previous deletion, should not impact flow
            }
            return response;
        }

        // If DI failed, retrieve data from cold-storage, remove previous data from ongoing storage system,
        ResponseEntity<IResponse> coldStorageResponse = objectService.getObjectByHash(objectHash, true);
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
            ResponseEntity<IResponse> deleteResponse =  objectService.deleteObjectByHash(objectHash, false);
            if ( isResponseOK(deleteResponse) )
            {   // Copy data from cold-storage to hot-storage
                ResponseEntity<IResponse> insertResponse = objectService.insertObjectJson(coldObjectHash, coldObjectAsJson, false);
                // TODO consider verifying response for possible retry mechanism
            }
        }
        return coldStorageResponse;
    }


    private ResponseEntity<IResponse> validateRetrieveObjectToStorage(Hash hash, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        List<Hash> hashToObjectJsonStringList = new ArrayList<>();
        hashToObjectJsonStringList.add(hash);

        ResponseEntity<IResponse> responsesEntity =
                historyNodesConsensusService.validateRetrieveMultipleObjectsConsensus(hashToObjectJsonStringList, historyNodeConsensusResult);
        return responsesEntity;
    }


    @Override
    public ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        // Validation of the request itself
        ResponseEntity<IResponse> response = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap, historyNodeConsensusResult);
        if( !isResponseOK(response) )
            return response;

        // Store data from approved request into ongoing storage
        response = objectService.insertMultiObjects(hashToObjectJsonDataMap, false); // TODO use additional param to indicate it is for ongoing storage
        if( !isResponseOK(response) )
            return response; // TODO consider some retry mechanism
        else
        {
            response = objectService.insertMultiObjects(hashToObjectJsonDataMap, true); // TODO use additional param to indicate it is for cold storage
            if( !isResponseOK(response) )
                return response; // TODO consider some retry mechanism, consider removing from ongoing storage
        }
        return response;
    }

    private ResponseEntity<IResponse> validateStoreObjectToStorage(Hash hash, String objectJson, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        Map<Hash, String> hashToObjectJsonDataMap = new HashMap<>();
        hashToObjectJsonDataMap.put(hash, objectJson);
        // Validate consensus decision on provided data from history nodes' master + Data Integrity
        ResponseEntity<IResponse> response = validateStoreMultipleObjectsToStorage(hashToObjectJsonDataMap, historyNodeConsensusResult);
        return response;
    }


    private ResponseEntity<IResponse> validateStoreMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        // Validate consensus decision on provided Transactions data from history nodes' master
        ResponseEntity<IResponse> response = historyNodesConsensusService.validateStoreMultipleObjectsConsensus(hashToObjectJsonDataMap, historyNodeConsensusResult);

        // Additional Validations after consensus checks - Data integrity
        if( !hashToObjectJsonDataMap.entrySet().stream().allMatch( entry -> isObjectDIOK(entry.getKey(), entry.getValue()) ) )
        {
            response = new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        return response;
    }


    @Override
    public Map<Hash, ResponseEntity<IResponse>> retrieveMultipleObjectsFromStorage(List<Hash> hashes, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        Map<Hash, ResponseEntity<IResponse>> responsesMap = new HashMap<>();
        // Validate consensus decision on the request for provided Address data from history nodes' master
        ResponseEntity<IResponse> iResponse = historyNodesConsensusService.validateRetrieveMultipleObjectsConsensus(hashes, historyNodeConsensusResult);
        if( !isResponseOK(iResponse) )
        {
            responsesMap.put(null, iResponse);
            return responsesMap;
        }

        // Retrieve data from ongoing storage system
        ResponseEntity<IResponse> objectsByHashResponse = objectService.getMultiObjectsFromDb(hashes, false);

        if( !isResponseOK(objectsByHashResponse) )
        {
            responsesMap.put(null, objectsByHashResponse);
            return responsesMap;
        }

        // For successfully retrieved data, perform also data-integrity checks
        ((Map<Hash, String>)objectsByHashResponse.getBody()).forEach( (hash, objectAsJsonString) ->
                {
                    responsesMap.put(hash, verifyRetrievedSingleObject(hash, objectAsJsonString, false));
                }
        );
        return responsesMap;
    }


    protected boolean isResponseOK(ResponseEntity<IResponse> iResponse) {
        return iResponse != null && iResponse.getStatusCode().equals(HttpStatus.OK);
    }

}
