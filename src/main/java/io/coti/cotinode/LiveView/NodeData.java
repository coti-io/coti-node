package io.coti.cotinode.LiveView;

import lombok.Data;

@Data
public class NodeData {
    private String id;
    private String leftParent;
    private String rightParent;
    private double trustScore;
    private int status;


    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof NodeData)) {
            return false;
        }
        return id.equals(((NodeData) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
