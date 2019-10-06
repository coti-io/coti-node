package io.coti.nodemanager.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class SingleNodeDetails {

    private Hash nodeHash;
    private String httpAddress;
    private String url;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FeeData feeData;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double trustScore;

    public SingleNodeDetails(Hash nodeHash, String fullHttpAddress, String webServerUrl) {
        this.nodeHash = nodeHash;
        this.httpAddress = fullHttpAddress;
        this.url = webServerUrl;
    }
}
