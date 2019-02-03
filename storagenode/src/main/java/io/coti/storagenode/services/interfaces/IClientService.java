package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;

import java.io.IOException;
import java.util.Set;

public interface IClientService {
    void getClusterDetails(Set<String> indexes) throws IOException;

    String getObjectFromDbByHash(Hash hash, String index) throws IOException;

    String insertObjectToDb(Hash hash, String objectAsJsonString, String index, String objectName) throws IOException;
}
