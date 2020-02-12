package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.NodeDailyActivityResponseData;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class GetNodeActivityInSecondsPerDaysResponse extends BaseResponse {

    private Map<LocalDate, NodeDailyActivityResponseData> upTimesByDates;

    public GetNodeActivityInSecondsPerDaysResponse(Map<LocalDate, NodeDailyActivityResponseData> upTimesByDates) {
        this.upTimesByDates = upTimesByDates;
    }
}
