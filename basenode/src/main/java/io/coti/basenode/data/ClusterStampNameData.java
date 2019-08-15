package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class ClusterStampNameData implements IEntity {

    private String name;

    private Hash hash;

    public ClusterStampType getType(){
        return name.substring(0,1) == "m" ? ClusterStampType.MAJOR : ClusterStampType.TOKEN;
    }


    @Override
    public Hash getHash() {
         return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }
}
