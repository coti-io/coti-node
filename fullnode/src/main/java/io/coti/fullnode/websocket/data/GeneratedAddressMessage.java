package io.coti.fullnode.websocket.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class GeneratedAddressMessage {

    private String addressHash;

    public GeneratedAddressMessage(Hash addressHash) {
        this.addressHash = addressHash.toHexString();
    }
}
