package io.coti.basenode.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ClusterStampType {
    CURRENCY("C"),
    BALANCE("B");

    private String mark;

    private static final class ClusterStampTypes {
        private static final Map<String, ClusterStampType> markToClusterStampTypeMap = new HashMap<>();
    }

    ClusterStampType(String mark) {
        this.mark = mark;
        ClusterStampTypes.markToClusterStampTypeMap.put(mark, this);
    }

    public String getMark() {
        return mark;
    }

    public static Optional<ClusterStampType> getTypeByMark(String clusterStampTypeMark) {
        return Optional.ofNullable(ClusterStampTypes.markToClusterStampTypeMap.get(clusterStampTypeMark));
    }
}
