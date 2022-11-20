package io.coti.nodemanager.websocket.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeMonitorService;
import lombok.Data;

@Data
public class NotifyNodeHealthStateChange {

    private Hash nodeHash;
    private BaseNodeMonitorService.HealthState reportedHealthState;

    public NotifyNodeHealthStateChange(Hash nodeHash, BaseNodeMonitorService.HealthState reportedHealthState) {
        this.nodeHash = nodeHash;
        this.reportedHealthState = reportedHealthState;
    }
}
