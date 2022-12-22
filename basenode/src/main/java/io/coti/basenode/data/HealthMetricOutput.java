package io.coti.basenode.data;

import lombok.Data;

@Data
public class HealthMetricOutput {

    private HealthMetricOutputType type;
    private String label;
    private Long value;

    public HealthMetricOutput(HealthMetricOutputType type, String label, long value) {
        this.type = type;
        this.label = label;
        this.value = value;
    }

}
