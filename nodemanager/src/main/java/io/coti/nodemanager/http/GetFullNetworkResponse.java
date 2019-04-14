package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetFullNetworkResponse extends BaseResponse {
    private Map<String, List<String>> fullNetworkResponse;

    public GetFullNetworkResponse(Map<String, List<String>> fullNetworkResponse) {
        this.fullNetworkResponse = fullNetworkResponse;
    }
}
