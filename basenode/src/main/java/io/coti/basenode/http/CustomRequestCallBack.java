package io.coti.basenode.http;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;

import java.io.IOException;

public class CustomRequestCallBack implements RequestCallback {

    private HttpJacksonSerializer jacksonSerializer;
    private SeriazableRequest request;

    public CustomRequestCallBack(HttpJacksonSerializer jacksonSerializer, SeriazableRequest request) {
        this.jacksonSerializer = jacksonSerializer;
        this.request = request;
    }

    @Override
    public void doWithRequest(ClientHttpRequest clientHttpRequest) throws IOException {
        clientHttpRequest.getBody().write(jacksonSerializer.serialize(request));
    }

}
