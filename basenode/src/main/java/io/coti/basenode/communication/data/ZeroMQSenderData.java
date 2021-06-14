package io.coti.basenode.communication.data;

import io.coti.basenode.data.interfaces.IPropagatable;

public class ZeroMQSenderData {
    private IPropagatable data = null;

    public IPropagatable getData() {
        return this.data;
    }

    public String getAddress() {
        return this.address;
    }

    private String address = null;

    public ZeroMQSenderData(IPropagatable data, String address) {
        this.data = data;
        this.address = address;
    }
}
