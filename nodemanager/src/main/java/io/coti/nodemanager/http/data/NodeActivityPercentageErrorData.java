package io.coti.nodemanager.http.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NodeActivityPercentageErrorData extends NodeActivityPercentageData {

    private String errorMessage;

    public NodeActivityPercentageErrorData(String errorMessage) {
        super();
        this.status = false;
        this.percentage = 0;
        this.errorMessage = errorMessage;
    }
}
