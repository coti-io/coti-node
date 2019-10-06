package io.coti.nodemanager.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class SingleNodeDetailsForWallet {

    private String nodeHash;
    private String httpAddress;
    private String url;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FeeData feeData;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double trustScore;

    public SingleNodeDetailsForWallet(Hash nodeHash, String fullHttpAddress, String webServerUrl) {
        this.nodeHash = nodeHash.toString();
        this.httpAddress = fullHttpAddress;
        this.url = webServerUrl;
    }
}
