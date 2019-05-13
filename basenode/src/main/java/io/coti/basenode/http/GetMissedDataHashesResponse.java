package io.coti.basenode.http;

import io.coti.basenode.data.MessageArrivalValidationData;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GetMissedDataHashesResponse extends Response {

    @NotNull
    @Valid
    private MessageArrivalValidationData messageArrivalValidationData;

    private GetMissedDataHashesResponse() {
    }

    public GetMissedDataHashesResponse(MessageArrivalValidationData messageArrivalValidationData, String message, String status) {
        super(message, status);
        this.messageArrivalValidationData = messageArrivalValidationData;
    }

}
