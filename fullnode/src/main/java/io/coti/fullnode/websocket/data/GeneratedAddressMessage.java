package io.coti.fullnode.websocket.data;

import io.coti.basenode.data.Hash;

public class GeneratedAddressMessage {

    public String addressHash;

    public GeneratedAddressMessage(Hash addressHash) {
        this.addressHash = addressHash.toHexString();
    }
}
