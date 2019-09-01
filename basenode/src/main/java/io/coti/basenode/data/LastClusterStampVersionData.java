package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LastClusterStampVersionData implements IEntity {

    @NotNull
    private Long versionTime;
    @NotNull
    private Hash hash;

    public LastClusterStampVersionData(Long versionTime, Hash hash){
        this.versionTime = versionTime;
        this.hash = hash;
    }
}
