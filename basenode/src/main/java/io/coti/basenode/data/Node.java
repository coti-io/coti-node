package io.coti.basenode.data;

import lombok.Data;

@Data
public class Node {
    NodeType nodeType;
    String address;
    String httpPort;
    String propagationPort;
    String ReceivingPort;

    public Node(NodeType nodeType, String address, String httpPort){
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
    }

    public Node(){}
}