package io.coti.basenode.http;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Slf4j
public class CustomHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    public CustomHttpComponentsClientHttpRequestFactory() {
        setHttpClient(HttpClients.createDefault());
    }

}
