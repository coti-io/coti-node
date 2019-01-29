package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
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
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


@Slf4j
@Data
@Service
public class ClientService {
    //private String INDEX_NAME = "transaction";
    private String INDEX_TYPE = "json";
    private String ELASTICSEARCH_HOST_IP = "localhost";
    private int ELASTICSEARCH_HOST_PORT1 = 9200;
    //private int ELASTICSEARCH_HOST_PORT2 = 9201;

    private RestHighLevelClient restClient;
    private ObjectMapper mapper;

    public void init(Map<String, String> indexes) {
        try {
            mapper = new ObjectMapper();
            restClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT1)
            ));
            for(Map.Entry<String, String> indexToObjectPair: indexes.entrySet()){
                if (!ifIndexExist(indexToObjectPair.getKey())) {
                    sendCreateIndexRequest(indexToObjectPair.getKey());
                    createMapping(indexToObjectPair.getKey(), indexToObjectPair.getValue());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void getClusterDetails(List<String> indexes) throws IOException {
        MainResponse mainResponse = restClient.info(RequestOptions.DEFAULT);

        for(String index : indexes){
            String searchShardsDetails = getSearchShardsDetails(index);
            ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest(index);
            ClusterHealthResponse response = restClient.cluster().health(clusterHealthRequest, RequestOptions.DEFAULT);
        }

        ClusterGetSettingsRequest clusterGetSettingsRequest = new ClusterGetSettingsRequest();
        ClusterGetSettingsResponse getSettings = restClient.cluster().getSettings(clusterGetSettingsRequest, RequestOptions.DEFAULT);
    }

    private String getSearchShardsDetails(String index) {
        final String uri = "http://" +  ELASTICSEARCH_HOST_IP + ":" + ELASTICSEARCH_HOST_PORT1 + "/" + index + "/" + "_search_shards";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);
        return result;
    }


    public String getTransactionByHash(Hash hash, String index) throws IOException {

        GetRequest request = new GetRequest(index, INDEX_TYPE, hash.toString());
        try {
            GetResponse getResponse = restClient.get(request, RequestOptions.DEFAULT);
            return getResponse.getSourceAsString();
        } catch (ElasticsearchException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void insertTransaction(Hash hash, String objectAsJsonString, String index, String objectName) throws IOException {
        IndexResponse indexResponse;
        try {
            IndexRequest request = new IndexRequest(
                    index,
                    INDEX_TYPE,
                    hash.toString());

            request.source((jsonBuilder()
                    .startObject()
                    .field(objectName, objectAsJsonString)
                    .endObject()));
            indexResponse = restClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private void sendCreateIndexRequest(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 1)
        );

        restClient.indices().create(request, RequestOptions.DEFAULT);
    }

    private void createMapping(String index, String objectName) throws IOException {

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
        AcknowledgedResponse putMappingResponse = restClient.indices().putMapping(request, RequestOptions.DEFAULT);
    }


    private boolean ifIndexExist(String indexName) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(indexName);
        boolean exists = restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        return exists;
    }
}
