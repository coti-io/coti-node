package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Map;

@Data
public class GetNodeActivityInSecondsPerDaysResponse extends BaseResponse {

    private Map<LocalDate, Pair> upTimesByDates;

    public GetNodeActivityInSecondsPerDaysResponse(Map<LocalDate, Pair> upTimesByDates) {
        this.upTimesByDates = upTimesByDates;
    }
}
