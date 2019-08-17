package io.coti.basenode.constants;

import io.coti.basenode.data.NetworkType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BaseNodeConstantInjector {

    public final NetworkType NETWORK_TYPE;
    public final String NODE_IP;
    public final String NODE_MANAGER_IP;

    private BaseNodeConstantInjector(@Value("${network}") final NetworkType NETWORK_TYPE,
                                     @Value("${server.ip}") final String NODE_IP,
                                     @Value("${node.manager.ip}") final String NODE_MANAGER_IP) {
        this.NETWORK_TYPE = NETWORK_TYPE;
        this.NODE_IP = NODE_IP;
        this.NODE_MANAGER_IP = NODE_MANAGER_IP;
    }
}
