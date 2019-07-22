package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class HeartBeatData implements IPropagatable {

    private Hash serverAddress;

    private HeartBeatData() {

    }

    public HeartBeatData(String serverAddress) {
        this.serverAddress = new Hash(serverAddress.getBytes());
    }

    @Override
    @JsonIgnore
    public Hash getHash() {
        return serverAddress;
    }

    @Override
    public void setHash(Hash hash) {
        this.serverAddress = hash;
    }
}
