package io.coti.fullnode.http;

import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class MessageArrivalValidationResponse  extends BaseResponse {

    private MessageArrivalValidationData messageArrivalValidationData;

    public MessageArrivalValidationResponse(MessageArrivalValidationData messageArrivalValidationData) {
        super();
        this.messageArrivalValidationData = messageArrivalValidationData;
    }

    public MessageArrivalValidationResponse(MessageArrivalValidationData messageArrivalValidationData, String status ) {
        super(status);
        this.messageArrivalValidationData = messageArrivalValidationData;
    }

}