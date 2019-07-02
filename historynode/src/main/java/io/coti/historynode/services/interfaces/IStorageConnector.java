package io.coti.historynode.services.interfaces;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Request;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IStorageConnector {
    <T extends Request> ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, T request);
    <T extends Request,U extends BaseResponse> ResponseEntity<IResponse> postForObject(String url, Class<ResponseEntity> responseEntityClass, T request);

    void putObject(String url, Request request);
}
