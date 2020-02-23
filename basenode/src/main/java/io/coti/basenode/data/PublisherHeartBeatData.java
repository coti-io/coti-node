package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

import java.time.Instant;

@Data
public class PublisherHeartBeatData implements IPropagatable {

    private Instant heartbeatTime;
    private String serverAddress;

    private PublisherHeartBeatData() {

    }

    public PublisherHeartBeatData(String serverAddress) {
        this.heartbeatTime = Instant.now();
        this.serverAddress = serverAddress;
    }

    @Override
    @JsonIgnore
    public Hash getHash() {
        return new Hash(heartbeatTime.toEpochMilli());
    }

    @Override
    public void setHash(Hash hash) {
        //no implementation
    }
}
