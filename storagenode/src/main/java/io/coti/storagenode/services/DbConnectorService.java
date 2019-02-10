package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.storagenode.data.MultiDbInsertionStatus;
import io.coti.storagenode.services.interfaces.IDbConnectorService;
import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
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

    private RestHighLevelClient restClientToMainStorage;
    private RestHighLevelClient restClientToSecondaryStorage;
    private ObjectMapper mapper;

    @PostConstruct
    private void init() {
        try {
            mapper = new ObjectMapper();
            restClientToMainStorage = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT1)

            ));
            restClientToSecondaryStorage = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT2)
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void addIndexes(Map<String, String> indexes, boolean addToMainStorage) throws IOException {
        for (Map.Entry<String, String> indexToObjectPair : indexes.entrySet()) {
            addIndex(indexToObjectPair.getKey(), indexToObjectPair.getValue(),  addToMainStorage);
        }
    }

    public void addIndex(String indexName, String objectName, boolean addToMainStorage) throws IOException {
        if (!ifIndexExist(indexName,  addToMainStorage)) {
            sendCreateIndexRequest(indexName,addToMainStorage);
            createMapping(indexName, objectName,  addToMainStorage);
        }
    }

    @Override
    public void getClusterDetails(Set<String> indexes, boolean mainStorageDetails) throws IOException {
        MainResponse mainResponse =  getStorage(mainStorageDetails).info(RequestOptions.DEFAULT);

        for (String index : indexes) {
            String searchShardsDetails = getSearchShardsDetails(index);
            ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest(index);
            ClusterHealthResponse response = getStorage(mainStorageDetails).cluster().health(clusterHealthRequest, RequestOptions.DEFAULT);
        }

        ClusterGetSettingsRequest clusterGetSettingsRequest = new ClusterGetSettingsRequest();
        ClusterGetSettingsResponse getSettings = getStorage(mainStorageDetails).cluster().getSettings(clusterGetSettingsRequest, RequestOptions.DEFAULT);
    }

    public HttpStatus getHttpStatus(MultiDbInsertionStatus multiDbInsertionStatus) {
        if (multiDbInsertionStatus == MultiDbInsertionStatus.Success) {
            return HttpStatus.OK;
        } else if (multiDbInsertionStatus == MultiDbInsertionStatus.Failed) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.MULTI_STATUS;
    }

    @Override
    public String getObjectFromDbByHash(Hash hash, String index, boolean getFromMainStorage) throws IOException {
        GetRequest request = new GetRequest(index, INDEX_TYPE, hash.toString());
        try {
            GetResponse getResponse = getStorage(getFromMainStorage).get(request, RequestOptions.DEFAULT);
            return getResponse.getSourceAsString();
        } catch (ElasticsearchException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Map<Hash, String> getMultiObjects(List<Hash> hashes, String indexName, boolean getFromMainStorage) throws Exception {
        Map<Hash, String> hashToObjectsFromDbMap = null;
        MultiGetResponse multiGetResponse = getMultiObjectsFromDb(hashes, indexName, getFromMainStorage);
        hashToObjectsFromDbMap = new HashMap<>();
        for (MultiGetItemResponse multiGetItemResponse : multiGetResponse.getResponses()) {
            hashToObjectsFromDbMap.put(new Hash(multiGetItemResponse.getId()),
                    new String(multiGetItemResponse.getResponse().getSourceAsBytes()));
        }
        return hashToObjectsFromDbMap;
    }

    @Override
    public String deleteObject(Hash hash, String indexName, boolean deleteFromMainStorage) {
        DeleteRequest request = new DeleteRequest(
                indexName,
                INDEX_TYPE,
                hash.toString());
        try {
            DeleteResponse deleteResponse = getStorage(deleteFromMainStorage).delete(
                    request, RequestOptions.DEFAULT);
            return deleteResponse.status().name();
        } catch (IOException e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

    @Override
    public Pair<MultiDbInsertionStatus, Map<Hash, String>> insertMultiObjectsToDb(String indexName, String objectName, Map<Hash, String> hashToObjectJsonDataMap, boolean addToMainStorage) throws Exception {
        Pair<MultiDbInsertionStatus, Map<Hash, String>> insertResponse = null;
        try {
            BulkRequest request = new BulkRequest();
            for (Map.Entry<Hash, String> entry : hashToObjectJsonDataMap.entrySet()) {
                request.add(new IndexRequest(indexName).id(entry.getKey().toString()).type(INDEX_TYPE)
                        .source(XContentType.JSON, objectName, entry.getValue()));
            }
            BulkResponse bulkResponse = getStorage(addToMainStorage).bulk(request, RequestOptions.DEFAULT);
            insertResponse = createMultiInsertResponse(bulkResponse);
            if (insertResponse.getValue().size() == hashToObjectJsonDataMap.size()) {
                insertResponse = new Pair<>(MultiDbInsertionStatus.Failed, insertResponse.getValue());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
        return insertResponse;

    }



    @Override
    public String insertObjectToDb(Hash hash, String objectAsJsonString, String index, String objectName, boolean addToMainStorage) {
        IndexResponse indexResponse = null;
        try {
            IndexRequest request = new IndexRequest(
                    index,
                    INDEX_TYPE,
                    hash.toString());
            request.source((jsonBuilder()
                    .startObject()
                    .field(objectName, objectAsJsonString)
                    .endObject()));
            indexResponse =  getStorage(addToMainStorage).index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            return indexResponse.toString();
        }
    }



    private void sendCreateIndexRequest(String index, boolean toMainStorage) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 2)
        );

        getStorage(toMainStorage).indices().create(request, RequestOptions.DEFAULT);
    }

    private MultiGetResponse getMultiObjectsFromDb(List<Hash> hashes, String indexName, boolean getFromMainStorage) throws Exception {
        MultiGetResponse multiGetResponse = null;
        try {
            MultiGetRequest request = new MultiGetRequest();
            for (Hash hash : hashes) {
                request.add(new MultiGetRequest.Item(
                        indexName,
                        INDEX_TYPE,
                        hash.toString()));
            }
            multiGetResponse =  getStorage(getFromMainStorage).mget(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
        return multiGetResponse;

    }

    private void createMapping(String index, String objectName, boolean isMainStorage) throws IOException {
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
        request.type(INDEX_TYPE);
        request.source(builder);
        getStorage(isMainStorage).indices().putMapping(request, RequestOptions.DEFAULT);
    }


    private boolean ifIndexExist(String indexName, boolean isMainStorage) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(indexName);
        return getStorage(isMainStorage).indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    private String getSearchShardsDetails(String index) {
        final String uri = "http://" + ELASTICSEARCH_HOST_IP + ":" + ELASTICSEARCH_HOST_PORT1 + "/" + index + "/" + "_search_shards";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(uri, String.class);
    }


    private Pair<MultiDbInsertionStatus, Map<Hash, String>> createMultiInsertResponse(BulkResponse bulkResponse) {
        if (bulkResponse == null) {
            return null;
        }
        MultiDbInsertionStatus errorInInsertion = MultiDbInsertionStatus.Success;
        Map<Hash, String> hashToResponseMap = new HashMap<>();
        for (BulkItemResponse bulkItemResponse : bulkResponse.getItems()) {
            if (bulkItemResponse.isFailed()) {
                errorInInsertion = MultiDbInsertionStatus.PartlyFailed;
                hashToResponseMap.put(new Hash(bulkItemResponse.getId()), bulkItemResponse.getFailure().getMessage());
            }
        }
        return new Pair<>(errorInInsertion, hashToResponseMap);
    }

    private RestHighLevelClient getStorage(boolean isMainStorage) {
        if (isMainStorage){
            return restClientToMainStorage;

        }
        return restClientToSecondaryStorage;
    }
}
