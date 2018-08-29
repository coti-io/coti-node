package io.coti.basenode.http.websocket;

import io.coti.basenode.data.Hash;

public class GeneratedAddressMessage {
    public String addressHash;

    public GeneratedAddressMessage(Hash addressHash) {
        this.addressHash = addressHash.toHexString();
    }
}
