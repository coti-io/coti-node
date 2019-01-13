package io.coti.financialserver.http;

import io.coti.financialserver.http.data.GetUnreadEventsData;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetUnreadEventsRequest {
    @NotNull
    private GetUnreadEventsData unreadEventsData;
}
