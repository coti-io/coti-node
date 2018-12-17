package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IPropagatable;

import java.util.List;

public interface ISender {

    void init(List<String> receivingServerAddresses);

    <T extends IPropagatable> void send(T toSend, String address);
}
