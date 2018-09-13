package io.coti.basenode.http;

import lombok.Data;

@Data
public class NodeProperties {
    String propagationAddress;
    String receivingAddress;
    String recoveryAddress;
}
