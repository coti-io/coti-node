package io.coti.cotinode.model;

import io.coti.cotinode.data.PreBalanceDifferenceData;
import lombok.Data;

@Data
public class PreBalanceDifferences extends Collection<PreBalanceDifferenceData> {

    public PreBalanceDifferences() {
    }

    public void init() {
        super.init();
    }
}