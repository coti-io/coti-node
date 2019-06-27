package io.coti.storagenode.database.interfaces;

import io.coti.basenode.data.Hash;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface IDbConnectorService {
    ClusterGetSettingsResponse getClusterDetails(Set<String> indexes) throws IOException;

    Map<String, Object> getObjectFromDbByHash(Hash hash, String index, boolean fromColdStorage) throws IOException;

    String insertObjectToDb(Hash hash, String objectAsJsonString, String index, String objectName, boolean fromColdStorage) throws IOException;

}
