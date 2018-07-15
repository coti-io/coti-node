package io.coti.zero_spend.http;

import io.coti.common.data.Hash;
import lombok.Data;

@Data
public class MonitorElement {
    private int count;
    private Hash nodeHash;

    public MonitorElement(Hash nodeHash, int count){
        this.count = count;
        this.nodeHash = nodeHash;
    }

}
