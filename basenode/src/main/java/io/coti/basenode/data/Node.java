package io.coti.basenode.data;

import lombok.Data;

import java.util.Objects;

@Data
public class Node {
    private NodeType nodeType;
    private String address;
    private String httpPort;
    private String propagationPort;
    private String receivingPort;
    private Hash hash;
    private String recoveryServerAddress;

    public Node(NodeType nodeType, String address, String httpPort) {
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
    }

    public Node() {
    }

    @Override
    public boolean equals(Object a) {
        if (a instanceof Node) {
            Node aNode = (Node) a;
            return nodeType.equals(aNode.nodeType) && address.equals(aNode.address) && httpPort.equals(aNode.httpPort);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        return Objects.hash(nodeType, address, httpPort);
    }

    public String getHttpFullAddress(){
        return "http://" + address + ":" + httpPort;
    }

    public String getPropagationFullAddress(){
        return "tcp://" + address + ":" + propagationPort;
    }

    public String getReceivingFullAddress(){
        return "tcp://" + address + ":" + receivingPort;
    }
}