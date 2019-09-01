package io.coti.basenode.data;

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
}
