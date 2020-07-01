package io.coti.basenode.http;

import io.coti.basenode.data.NodeRegistrationData;
import lombok.Data;

@Data
public class GetNetworkVotersRequest extends Request {

    private NodeRegistrationData nodeRegistrationData;

    public GetNetworkVotersRequest() {
    }

    public GetNetworkVotersRequest(NodeRegistrationData nodeRegistrationData) {
        this.nodeRegistrationData = nodeRegistrationData;
    }

}
