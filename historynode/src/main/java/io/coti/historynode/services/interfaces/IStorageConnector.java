package io.coti.historynode.services.interfaces;

import io.coti.basenode.http.Request;
import io.coti.basenode.http.Response;
import org.springframework.http.ResponseEntity;

//TODO 7/9/2019 astolia: check if changing  <T extends Request, U extends Response> to <T, U> works fine
public interface IStorageConnector<T extends Request, U extends Response> {

    ResponseEntity<U> getForObject(String url, T request);
    ResponseEntity<U> postForObjects(String url, T request);

    ResponseEntity<U> getForObject(String url, T request, Class<U> responseType);
    ResponseEntity<U> postForObjects(String url, T request, Class<U> responseType);

    ResponseEntity<U> storeInStorage(String url, T request, Class<U> responseType);

}
