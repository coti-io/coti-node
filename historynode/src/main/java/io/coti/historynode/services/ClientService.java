package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.TRANSACTION_ALREADY_EXIST_MESSAGE;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


@Slf4j
@Data
@Service
public class ClientService {
    String t = "";
    private String INDEX_NAME = "transaction";
   private String INDEX_TYPE = "json";
    private String ELASTICSEARCH_HOST_IP = "localhost";
    private int ELASTICSEARCH_HOST_PORT1 = 9200;
    private int ELASTICSEARCH_HOST_PORT2 = 9201;

    private RestHighLevelClient restClient;
    private ObjectMapper mapper;

    public void init() {
        try {
            mapper = new ObjectMapper();
            restClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT1),
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT2)));
            if (!ifIndexExist(INDEX_NAME)) {
                sendCreateIndexRequest();
                createMapping();
            }
            //createMapping();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void sendCreateIndexRequest() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 6)
                .put("index.number_of_replicas", 2)
        );

        //request.source(createMapping());
        restClient.indices().create(request, RequestOptions.DEFAULT);
    }

    private void createMapping() throws IOException {

        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
//                builder.startObject("hash");
//                {
//                    builder.field("type", "text");
//                }
               // builder.endObject();
                builder.startObject("transactionData");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        PutMappingRequest request = new PutMappingRequest(INDEX_NAME);
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

    public boolean validateTransaction(String transactionAsJsonString) throws IOException {
        // TODO:
        return true;
    }

    public String getTransactionByHash(Hash hash) throws IOException {

        GetRequest request = new GetRequest(INDEX_NAME, INDEX_TYPE, hash.toString());
        try {
            GetResponse getResponse = restClient.get(request, RequestOptions.DEFAULT);
            return getResponse.getSourceAsString();
        } catch (ElasticsearchException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void insertTransaction(Hash hash, String transactionAsJsonString) throws IOException {
        IndexResponse indexResponse;
        try {
            validateTransaction(transactionAsJsonString);
            IndexRequest request = new IndexRequest(
                    INDEX_NAME,
                    INDEX_TYPE,
                    hash.toString());

            request.source((jsonBuilder()
                    .startObject()
                    .field("transactionData", transactionAsJsonString)
                    .endObject()));
            indexResponse = restClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }
}
