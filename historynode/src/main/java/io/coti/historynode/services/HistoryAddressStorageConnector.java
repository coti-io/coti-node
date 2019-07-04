package io.coti.historynode.services;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.Request;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class HistoryAddressStorageConnector extends StorageConnector {

    @Override
    public <T extends Request> ResponseEntity<IResponse> getForObject(String url, Class<ResponseEntity> responseEntityClass, T request) {
        //TODO 7/1/2019 astolia: implement and check works
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<GetEntityRequest> requestEntity = new HttpEntity<>((GetEntityRequest)request, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<IResponse>> exchangeResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
                new ParameterizedTypeReference<List<IResponse>>() {
                });
        return (ResponseEntity<IResponse>) exchangeResponse.getBody().get(0);
    }

    @Override
    public <T extends Request, U extends BaseResponse> ResponseEntity<IResponse> postForObject(String url, Class<ResponseEntity> responseEntityClass, T request) {
        //TODO 7/2/2019 astolia: implement
        return null;
    }


}
