package io.coti.storagenode.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.storagenode.data.MultiDbInsertionStatus;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.interfaces.IDbConnectorService;
import io.coti.storagenode.exceptions.DbConnectorException;
import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


@Slf4j
@Data
@Service
public class DbConnectorService implements IDbConnectorService {
    private static final String INDEX_TYPE = "json";
    private static final String ELASTICSEARCH_HOST_IP = "localhost";
    private static final int ELASTICSEARCH_HOST_PORT1 = 9200;
    private static final int ELASTICSEARCH_HOST_PORT2 = 9201;

    private static final String ELASTICSEARCH_SECONDARY_HOST_IP = "localhost";
    private static final int ELASTICSEARCH_SECONDARY_HOST_PORT1 = 9200;
    private static final int ELASTICSEARCH_SECONDARY_HOST_PORT2 = 9201;
    public static final int INDEX_NUMBER_OF_SHARDS = 1;
    public static final int INDEX_NUMBER_OF_REPLICAS = 2;


    private RestHighLevelClient restClient;
    private RestHighLevelClient restColdStorageClient;
    private ObjectMapper mapper;

    @PostConstruct
    private void init() {
        try {
            mapper = new ObjectMapper();
            restClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT1),
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT2)
            ));
            restColdStorageClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(ELASTICSEARCH_SECONDARY_HOST_IP, ELASTICSEARCH_SECONDARY_HOST_PORT1),
                    new HttpHost(ELASTICSEARCH_SECONDARY_HOST_IP, ELASTICSEARCH_SECONDARY_HOST_PORT2)
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void addIndexes(boolean fromColdStorage) throws IOException {
        for (ElasticSearchData data : ElasticSearchData.values()) {
            addIndex(data.getIndex(), data.getObjectName(), fromColdStorage);
        }
    }

    private void addIndex(String indexName, String objectName, boolean fromColdStorage) throws IOException {
        if (!ifIndexExist(indexName, fromColdStorage)) {
            sendCreateIndexRequest(indexName, fromColdStorage);
            createMapping(indexName, objectName, fromColdStorage);
        }
    }

    @Override
    public ClusterGetSettingsResponse getClusterDetails(Set<String> indexes) throws IOException {
        MainResponse mainResponse = restClient.info(RequestOptions.DEFAULT);

        for (String index : indexes) {
            String searchShardsDetails = getSearchShardsDetails(index);
            ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest(index);
            ClusterHealthResponse response = restClient.cluster().health(clusterHealthRequest, RequestOptions.DEFAULT);
        }

        ClusterGetSettingsRequest clusterGetSettingsRequest = new ClusterGetSettingsRequest();
        ClusterGetSettingsResponse getSettings = restClient.cluster().getSettings(clusterGetSettingsRequest, RequestOptions.DEFAULT);
        return getSettings;
    }

    private String getSearchShardsDetails(String index) {
        final String uri = "http://" + ELASTICSEARCH_HOST_IP + ":" + ELASTICSEARCH_HOST_PORT1 + "/" + index + "/" + "_search_shards";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(uri, String.class);
    }


    @Override
    public GetResponse getObjectFromDbByHash(Hash hash, String index, boolean fromColdStorage) {
        GetRequest request = new GetRequest(index, hash.toString());

        try {
            GetResponse getResponse;
            if (fromColdStorage) {
                getResponse = restColdStorageClient.get(request, RequestOptions.DEFAULT);
            } else {
                getResponse = restClient.get(request, RequestOptions.DEFAULT);
            }
            return getResponse;
        } catch (Exception e) {
            throw new DbConnectorException(String.format("Error at get object from db by hash : {}", e.getMessage()));
        }
    }

    public Pair<MultiDbInsertionStatus, Map<Hash, String>> insertMultiObjectsToDb(String indexName, String objectName, Map<Hash, String> hashToObjectJsonDataMap, boolean fromColdStorage) throws Exception {
        Pair<MultiDbInsertionStatus, Map<Hash, String>> insertResponse;
        try {
            BulkRequest request = new BulkRequest();
            for (Map.Entry<Hash, String> entry : hashToObjectJsonDataMap.entrySet()) {
                request.add(new IndexRequest(indexName).id(entry.getKey().toString())
                        .source(XContentType.JSON, objectName, entry.getValue()));
//                        .source(entry.getValue(), XContentType.JSON ));
            }
            BulkResponse bulkResponse;
            if (fromColdStorage)
                bulkResponse = restColdStorageClient.bulk(request, RequestOptions.DEFAULT);
            else
                bulkResponse = restClient.bulk(request, RequestOptions.DEFAULT);

            insertResponse = createMultiInsertResponse(bulkResponse);
            if (insertResponse != null && (insertResponse.getValue().size() != hashToObjectJsonDataMap.size() || !insertResponse.getKey().equals(MultiDbInsertionStatus.Success))) {
                insertResponse = new Pair<>(MultiDbInsertionStatus.Failed, insertResponse.getValue());
            } else {
                insertResponse = new Pair<>(insertResponse.getKey(), insertResponse.getValue());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
        return insertResponse;

    }

    private Pair<MultiDbInsertionStatus, Map<Hash, String>> createMultiInsertResponse(BulkResponse bulkResponse) {
        if (bulkResponse == null) {
            return null;
        }
        MultiDbInsertionStatus insertionStatus = MultiDbInsertionStatus.Success;
        Map<Hash, String> hashToResponseMap = new HashMap<>();
        for (BulkItemResponse bulkItemResponse : bulkResponse.getItems()) {
            if (bulkItemResponse.isFailed()) {
                insertionStatus = MultiDbInsertionStatus.PartlyFailed;
                hashToResponseMap.put(new Hash(bulkItemResponse.getId()), bulkItemResponse.getFailure().getMessage());
            } else {
                hashToResponseMap.put(new Hash(bulkItemResponse.getId()), bulkItemResponse.getResponse().getResult().name());
            }

        }
        return new Pair<>(insertionStatus, hashToResponseMap);
    }

    private MultiGetResponse getMultiObjectsFromDb(List<Hash> hashes, String indexName, boolean fromColdStorage) {
        try {
            MultiGetResponse multiGetResponse;

            MultiGetRequest request = new MultiGetRequest();
            hashes.forEach(hash ->
                    request.add(new MultiGetRequest.Item(
                            indexName,
                            hash.toString()))
            );
            if (fromColdStorage) {
                multiGetResponse = restColdStorageClient.mget(request, RequestOptions.DEFAULT);
            } else {
                multiGetResponse = restClient.mget(request, RequestOptions.DEFAULT);
            }

            return multiGetResponse;
        } catch (IOException e) {
            throw new DbConnectorException(String.format("Error at get multi objects from db: {}", e.getMessage()));
        }

    }

    @Override
    public Map<Hash, String> getMultiObjects(List<Hash> hashes, String indexName, boolean fromColdStorage, String fieldName) {
        Map<Hash, String> hashToObjectsFromDbMap = new HashMap<>();
        MultiGetResponse multiGetResponse = getMultiObjectsFromDb(hashes, indexName, fromColdStorage);
        for (MultiGetItemResponse multiGetItemResponse : multiGetResponse.getResponses()) {
            if (multiGetItemResponse.getResponse().isExists()) {
                hashToObjectsFromDbMap.put(new Hash(multiGetItemResponse.getId()),
                        (String) multiGetItemResponse.getResponse().getSourceAsMap().get(fieldName));
            } else {
                hashToObjectsFromDbMap.put(new Hash(multiGetItemResponse.getId()), null);
            }
        }
        return hashToObjectsFromDbMap;
    }

    @Override
    public IndexResponse insertObjectToDb(Hash hash, String objectAsJsonString, String index, String objectName, boolean fromColdStorage) {
        IndexResponse indexResponse;
        try {
            IndexRequest request = new IndexRequest(index);
            request.id(hash.toString());
            request.source((jsonBuilder()
                    .startObject()
                    .field(objectName, objectAsJsonString)
                    .endObject()));
            if (fromColdStorage) {
                indexResponse = restColdStorageClient.index(request, RequestOptions.DEFAULT);
            } else {
                indexResponse = restClient.index(request, RequestOptions.DEFAULT);
            }
            return indexResponse;
        } catch (Exception e) {
            throw new DbConnectorException(String.format("Error at insert object to db: {}", e.getMessage()));
        }
    }

    public HttpStatus getHttpStatus(MultiDbInsertionStatus multiDbInsertionStatus) {
        if (multiDbInsertionStatus == MultiDbInsertionStatus.Success) {
            return HttpStatus.OK;
        } else if (multiDbInsertionStatus == MultiDbInsertionStatus.Failed) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.MULTI_STATUS;
    }

    private void sendCreateIndexRequest(String index, boolean fromColdStorage) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder()
                .put("index.number_of_shards", INDEX_NUMBER_OF_SHARDS)
                .put("index.number_of_replicas", INDEX_NUMBER_OF_REPLICAS)
        );

        if (fromColdStorage)
            restColdStorageClient.indices().create(request, RequestOptions.DEFAULT);
        else
            restClient.indices().create(request, RequestOptions.DEFAULT);
    }

    private void createMapping(String index, String objectName, boolean fromColdStorage) throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject(objectName);
                {
                    builder.field("type", "text");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        PutMappingRequest request = new PutMappingRequest(index);
//        request.type(INDEX_TYPE);
        request.source(builder);
        if (fromColdStorage)
            restColdStorageClient.indices().putMapping(request, RequestOptions.DEFAULT);
        else
            restClient.indices().putMapping(request, RequestOptions.DEFAULT);
    }


    private boolean ifIndexExist(String indexName, boolean fromColdStorage) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

        if (fromColdStorage)
            return restColdStorageClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        else
            return restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    public DeleteResponse deleteObject(Hash hash, String indexName, boolean fromColdStorage) {
        DeleteRequest request = new DeleteRequest(
                indexName,
                hash.toString());
        try {
            DeleteResponse deleteResponse;
            if (fromColdStorage)
                deleteResponse = restColdStorageClient.delete(request, RequestOptions.DEFAULT);
            else
                deleteResponse = restClient.delete(request, RequestOptions.DEFAULT);

            return deleteResponse;
        } catch (IOException e) {
            throw new DbConnectorException(e.getMessage());
        }
    }
}
