package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class ClusterStampNameData implements IEntity {

    private String name;

    private Hash hash;

    public ClusterStampType getType(){
        return name.substring(0,14) == "clusterstamp_m" ? ClusterStampType.MAJOR : ClusterStampType.TOKEN;
    }

    public ClusterStampNameData(String name){
        this.name = name;
    }
}
