package io.coti.basenode.data;

import java.util.Optional;

public enum ClusterStampType {
    MAJOR("M"),
    TOKEN("T");

    private String mark;

    ClusterStampType(String mark) {
        this.mark = mark;
    }

    public String getMark() {
        return mark;
    }

    public static Optional<ClusterStampType> getTypeByMark(String clusterStampTypeMark){
        if(clusterStampTypeMark.equals("M")){
            return Optional.of(ClusterStampType.MAJOR);
        }
        else if(clusterStampTypeMark.equals("T")){
            return Optional.of(ClusterStampType.TOKEN);
        }
        return Optional.empty();
    }
}
