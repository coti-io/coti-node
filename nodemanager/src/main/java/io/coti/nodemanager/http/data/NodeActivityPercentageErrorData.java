package io.coti.nodemanager.http.data;

import lombok.Data;

@Data
public class NodeActivityPercentageErrorData extends NodeActivityPercentageData {

    private String errorMessage;

    public NodeActivityPercentageErrorData(String errorMessage) {
        super();
        this.status = false;
        this.percentage = 0;
        this.errorMessage = errorMessage;
    }
}
