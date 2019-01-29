package io.coti.historynode.services.interfaces;

import io.coti.basenode.data.Hash;

import java.io.IOException;
import java.util.Set;

public interface IClientService {
    void getClusterDetails(Set<String> indexes) throws IOException;

    String getObjectByHash(Hash hash, String index) throws IOException;

    String insertObject(Hash hash, String objectAsJsonString, String index, String objectName) throws IOException;
}
