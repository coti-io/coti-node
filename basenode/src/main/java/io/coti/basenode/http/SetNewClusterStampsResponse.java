package io.coti.basenode.http;

import lombok.Data;

@Data
public class SetNewClusterStampsResponse extends BaseResponse {

    private String clusterStampFileName;

    public SetNewClusterStampsResponse() {
    }

    public SetNewClusterStampsResponse(String clusterStampFileName) {
        this.clusterStampFileName = clusterStampFileName;
    }
}
