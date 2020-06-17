package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetFullNetworkResponse extends BaseResponse {

    private Map<String, List<String>> fullNetworkResponse;

    public GetFullNetworkResponse(Map<String, List<String>> fullNetworkResponse) {
        this.fullNetworkResponse = fullNetworkResponse;
    }
}
