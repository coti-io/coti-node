package io.coti.fullnode.http;

import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.basenode.http.Response;
import lombok.Data;

@Data
public class MessageArrivalValidationResponse extends Response {

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

/*
@NotNull
@Valid
private MerchantRollingReserveAddressData merchantRollingReserveAddressData;

private GetMerchantRollingReserveAddressResponse() {
        }

public GetMerchantRollingReserveAddressResponse(String message, String status) {
        super(message, status);
        }

public GetMerchantRollingReserveAddressResponse(Hash merchantHash, Hash merchantRollingReserveAddress) {
        super();
        this.merchantRollingReserveAddressData = new MerchantRollingReserveAddressData(merchantHash, merchantRollingReserveAddress);
        }
        */