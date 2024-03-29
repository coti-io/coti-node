package io.coti.nodemanager.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HealthState;
import lombok.Data;

import java.io.Serializable;

@Data
public class SingleNodeDetailsForWallet implements Serializable {

    private String nodeHash;
    private String httpAddress;
    private String url;
    private String version;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FeeData feeData;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double trustScore;
    private HealthState reportedHealthState;

    public SingleNodeDetailsForWallet(Hash nodeHash, String fullHttpAddress, String webServerUrl, String version, HealthState reportedHealthState) {
        this.nodeHash = nodeHash.toString();
        this.httpAddress = fullHttpAddress;
        this.url = webServerUrl;
        this.version = version;
        this.reportedHealthState = reportedHealthState;
    }
}
