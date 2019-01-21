package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.TransactionData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


@Slf4j
@Data
@Service
public class ClientService {
    private String INDEX_NAME = "transaction";
    // private String TRANSACTION_TYPE_NAME = "transaction";
    private String ELASTICSEARCH_HOST_IP = "localhost";
    private int ELASTICSEARCH_HOST_PORT = 9200;

    private RestHighLevelClient restClient;
    private ObjectMapper mapper;

    public void init() {
        try {
            mapper = new ObjectMapper();
            restClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(ELASTICSEARCH_HOST_IP, ELASTICSEARCH_HOST_PORT)));
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
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1)
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
                builder.startObject("hash");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
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
        request.type("_doc");
        request.source(builder);
        AcknowledgedResponse putMappingResponse = restClient.indices().putMapping(request, RequestOptions.DEFAULT);
    }

    private boolean ifIndexExist(String indexName) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(indexName);
        boolean exists = restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        return exists;
    }

    public void insertTransaction(TransactionData transactionData) throws IOException {
        try {

            String transactionToJsonString = mapper.writeValueAsString(transactionData);
            IndexRequest request = new IndexRequest(
                    INDEX_NAME,
                    "_doc",
                    transactionData.getHash().toString());

            request.source((jsonBuilder()
                    .startObject()
                    .field("hash", transactionData.getHash().toString())
                    .field("transactionData", transactionToJsonString)
                    .endObject()));
            IndexResponse indexResponse =  restClient.index(request, RequestOptions.DEFAULT);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
