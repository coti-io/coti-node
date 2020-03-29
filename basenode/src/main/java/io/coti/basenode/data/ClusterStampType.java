package io.coti.basenode.data;

import java.util.Optional;

public enum ClusterStampType {
    MAJOR("M"),
    CURRENCIES("C");

    private String mark;

    ClusterStampType(String mark) {
        this.mark = mark;
    }

    public String getMark() {
        return mark;
    }

    public static Optional<ClusterStampType> getTypeByMark(String clusterStampTypeMark) {
        for (ClusterStampType clusterStampType : values()) {
            if (clusterStampType.getMark().equals(clusterStampTypeMark)) {
                return Optional.of(clusterStampType);
            }
        }
        return Optional.empty();
    }
}
