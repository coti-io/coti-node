package io.coti.basenode.http;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;

import java.io.IOException;

public class CustomRequestCallBack implements RequestCallback {

    private final HttpJacksonSerializer jacksonSerializer;
    private final SerializableRequest request;

    public CustomRequestCallBack(HttpJacksonSerializer jacksonSerializer, SerializableRequest request) {
        this.jacksonSerializer = jacksonSerializer;
        this.request = request;
    }

    @Override
    public void doWithRequest(ClientHttpRequest clientHttpRequest) throws IOException {
        clientHttpRequest.getHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        clientHttpRequest.getBody().write(jacksonSerializer.serialize(request));
    }

}
