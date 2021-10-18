package io.coti.historynode.services.interfaces;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IRequest;
import org.springframework.http.ResponseEntity;

public interface IStorageConnector<T extends IRequest, U extends BaseResponse> {

    ResponseEntity<U> retrieveFromStorage(String url, T request, Class<U> responseType);

    ResponseEntity<U> storeInStorage(String url, T request, Class<U> responseType);


}
