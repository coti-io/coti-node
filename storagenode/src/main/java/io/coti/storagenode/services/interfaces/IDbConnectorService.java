package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.storagenode.data.MultiDbInsertionStatus;
import javafx.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDbConnectorService {
    void getClusterDetails(Set<String> indexes, boolean getFromMainStorage) throws IOException;

    String getObjectFromDbByHash(Hash hash, String index, boolean getFromMainStorage) throws IOException;

    Map<Hash, String> getMultiObjects(List<Hash> hashes, String indexName, boolean getFromMainStorage) throws Exception;

    String insertObjectToDb(Hash hash, String objectAsJsonString, String index, String objectName, boolean addToMainStorage) throws IOException;

    Pair<MultiDbInsertionStatus, Map<Hash, String>> insertMultiObjectsToDb(String indexName,
                                                                           String objectName,
                                                                           Map<Hash, String> hashToObjectJsonDataMap,
                                                                           boolean addToMainStorage) throws Exception;

    String deleteObject(Hash hash, String indexName, boolean deleteFromMainStorage);
}
