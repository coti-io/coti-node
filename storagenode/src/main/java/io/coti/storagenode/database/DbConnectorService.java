package io.coti.storagenode.database;

import io.coti.basenode.data.Hash;
import io.coti.storagenode.data.MultiDbInsertionStatus;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.interfaces.IDbConnectorService;
import io.coti.storagenode.exceptions.DbConnectorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.xcontent.XContentFactory.jsonBuilder;


@Slf4j
@Service
public class DbConnectorService implements IDbConnectorService {

    public static final int INDEX_NUMBER_OF_SHARDS = 1;
    public static final int INDEX_NUMBER_OF_REPLICAS = 2;
    private final String elasticsearchHostIp;
    private final int elasticsearchHostPort1;
    private final int elasticsearchHostPort2;
    private final String elasticsearchSecondaryHostIp;
    private final int elasticsearchSecondaryHostPort1;
    private final int elasticsearchSecondaryHostPort2;
    private RestHighLevelClient restClient;
    private RestHighLevelClient restColdStorageClient;

    @Autowired
    private DbConnectorService(@Value("${elasticsearch.host.ip}") final String elasticsearchHostIp,
                               @Value("${elasticsearch.host.port1}") final int elasticsearchHostPort1,
                               @Value("${elasticsearch.host.port2}") final int elasticsearchHostPort2,
                               @Value("${elasticsearch.secondary.host.ip}") final String elasticsearchSecondaryHostIp,
                               @Value("${elasticsearch.secondary.host.port1}") final int elasticsearchSecondaryHostPort1,
                               @Value("${elasticsearch.secondary.host.port2}") final int elasticsearchSecondaryHostPort2

    ) {
        this.elasticsearchHostIp = elasticsearchHostIp;
        this.elasticsearchHostPort1 = elasticsearchHostPort1;
        this.elasticsearchHostPort2 = elasticsearchHostPort2;
        this.elasticsearchSecondaryHostIp = elasticsearchSecondaryHostIp;
        this.elasticsearchSecondaryHostPort1 = elasticsearchSecondaryHostPort1;
        this.elasticsearchSecondaryHostPort2 = elasticsearchSecondaryHostPort2;

    }

    public void init() {
        try {
            restClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(elasticsearchHostIp, elasticsearchHostPort1),
                    new HttpHost(elasticsearchHostIp, elasticsearchHostPort2)
            ));
            restColdStorageClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(elasticsearchSecondaryHostIp, elasticsearchSecondaryHostPort1),
                    new HttpHost(elasticsearchSecondaryHostIp, elasticsearchSecondaryHostPort2)
            ));
        } catch (Exception e) {
            throw new DbConnectorException(e.getMessage());
        }
    }

    public void addIndexes(boolean fromColdStorage) {
        for (ElasticSearchData data : ElasticSearchData.values()) {
            addIndex(data.getIndex(), data.getObjectName(), fromColdStorage);
        }
    }

    private void addIndex(String indexName, String objectName, boolean fromColdStorage) {
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
        return restClient.cluster().getSettings(clusterGetSettingsRequest, RequestOptions.DEFAULT);
    }

    private String getSearchShardsDetails(String index) {
        final String uri = "http://" + elasticsearchHostIp + ":" + elasticsearchHostPort1 + "/" + index + "/" + "_search_shards";
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
            throw new DbConnectorException(String.format("Error at get object from db by hash : %s", e.getMessage()));
        }
    }

    public BulkResponse insertMultiObjectsToDb(String indexName, String objectName, Map<Hash, String> hashToObjectJsonDataMap, boolean fromColdStorage) {

        try {
            BulkRequest request = new BulkRequest();
            for (Map.Entry<Hash, String> entry : hashToObjectJsonDataMap.entrySet()) {
                request.add(new IndexRequest(indexName).id(entry.getKey().toString())
                        .source(XContentType.JSON, objectName, entry.getValue()));
            }
            BulkResponse bulkResponse;
            if (fromColdStorage) {
                bulkResponse = restColdStorageClient.bulk(request, RequestOptions.DEFAULT);
            } else {
                bulkResponse = restClient.bulk(request, RequestOptions.DEFAULT);
            }

            return bulkResponse;
        } catch (IOException e) {
            throw new DbConnectorException(String.format("Error at insert multi objects to db: %s", e.getMessage()));
        }

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
            throw new DbConnectorException(String.format("Error at get multi objects from db: %s", e.getMessage()));
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
            throw new DbConnectorException(String.format("Error at insert object to db: %s", e.getMessage()));
        }
    }

    public HttpStatus getHttpStatus(MultiDbInsertionStatus multiDbInsertionStatus) {
        if (multiDbInsertionStatus == MultiDbInsertionStatus.SUCCESS) {
            return HttpStatus.OK;
        } else if (multiDbInsertionStatus == MultiDbInsertionStatus.FAILED) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.MULTI_STATUS;
    }

    private void sendCreateIndexRequest(String index, boolean fromColdStorage) {
        try {
            CreateIndexRequest request = new CreateIndexRequest(index);
            Settings.Builder builder = Settings.builder();
            request.settings(builder
                    .put("index.number_of_shards", INDEX_NUMBER_OF_SHARDS)
                    .put("index.number_of_replicas", INDEX_NUMBER_OF_REPLICAS)
            );

            if (fromColdStorage) {
                restColdStorageClient.indices().create(request, RequestOptions.DEFAULT);
            } else {
                restClient.indices().create(request, RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            throw new DbConnectorException(String.format("Error at creating index. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    private void createMapping(String index, String objectName, boolean fromColdStorage) {
        try {
            XContentBuilder builder = jsonBuilder();
            builder.startObject();
            buildObjectName(objectName, builder);
            builder.endObject();
            PutMappingRequest request = new PutMappingRequest(index);
            request.source();
            if (fromColdStorage) {
                restColdStorageClient.indices().putMapping(request, RequestOptions.DEFAULT);
            } else {
                restClient.indices().putMapping(request, RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            throw new DbConnectorException(String.format("Error at creating mapping. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    private void buildObjectName(String objectName, XContentBuilder builder) throws IOException {
        builder.startObject()
                .startObject("properties")
                .startObject(objectName)
                .field("type", "text")
                .endObject()
                .endObject()
                .endObject();
    }


    private boolean ifIndexExist(String indexName, boolean fromColdStorage) {

        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

            if (fromColdStorage) {
                return restColdStorageClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            } else {
                return restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            throw new DbConnectorException(String.format("Error at getting index. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    public DeleteResponse deleteObject(Hash hash, String indexName, boolean fromColdStorage) {
        DeleteRequest request = new DeleteRequest(
                indexName,
                hash.toString());
        try {
            DeleteResponse deleteResponse;
            if (fromColdStorage) {
                deleteResponse = restColdStorageClient.delete(request, RequestOptions.DEFAULT);
            } else {
                deleteResponse = restClient.delete(request, RequestOptions.DEFAULT);
            }

            return deleteResponse;
        } catch (IOException e) {
            throw new DbConnectorException(e.getMessage());
        }
    }
}
