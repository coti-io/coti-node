package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class LastClusterStampVersionData implements IEntity {

    private Long versionTimeMillis;
    private Hash hash;

    public LastClusterStampVersionData(Long versionTimeMillis) {
        this.versionTimeMillis = versionTimeMillis;
        this.hash = new Hash(versionTimeMillis);
    }
}
