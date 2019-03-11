package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;

import java.io.IOException;
import java.util.Set;

public interface IDbConnectorService {
    void getClusterDetails(Set<String> indexes) throws IOException;

    String getObjectFromDbByHash(Hash hash, String index, boolean fromColdStorage) throws IOException;

    String insertObjectToDb(Hash hash, String objectAsJsonString, String index, String objectName, boolean fromColdStorage) throws IOException;

}
