package io.coti.basenode.data;

public enum ClusterStampType {
    MAJOR,
    TOKEN;

    public String getPrefix(){
        return this.name().substring(0,1);
    }

}
