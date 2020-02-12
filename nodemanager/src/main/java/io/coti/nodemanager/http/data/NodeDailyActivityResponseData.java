package io.coti.nodemanager.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

@Data
public class NodeDailyActivityResponseData implements IResponseData {

    private long upTime;
    private long downTime;

    public NodeDailyActivityResponseData(long upTime, long downTime) {
        this.upTime = upTime;
        this.downTime = downTime;
    }

}
