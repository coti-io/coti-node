package io.coti.common.http.websocket;

import io.coti.common.data.Hash;

public class GeneratedAddressMessage {
    public String addressHash;

    public GeneratedAddressMessage(Hash addressHash) {
        this.addressHash = addressHash.toHexString();
    }
}
