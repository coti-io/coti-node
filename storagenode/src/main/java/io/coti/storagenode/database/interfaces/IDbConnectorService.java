package io.coti.storagenode.database.interfaces;

import io.coti.basenode.data.Hash;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDbConnectorService {
    ClusterGetSettingsResponse getClusterDetails(Set<String> indexes) throws IOException;

    GetResponse getObjectFromDbByHash(Hash hash, String index, boolean fromColdStorage);

    Map<Hash, String> getMultiObjects(List<Hash> hashes, String indexName, boolean fromColdStorage, String fieldName);

    IndexResponse insertObjectToDb(Hash hash, String objectAsJsonString, String index, String objectName, boolean fromColdStorage) throws IOException;

}
