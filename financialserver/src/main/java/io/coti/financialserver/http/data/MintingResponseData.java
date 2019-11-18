package io.coti.financialserver.http.data;

import io.coti.basenode.http.interfaces.ISerializable;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintingResponseData implements ISerializable {

    private String hash;
    private String mintingFeeWarrantHash;
    private Instant mintingRequestTime;
    protected BigDecimal amount;
    private String receiverAddress;
    private TokenServiceFeeResponseData tokenServiceFeeData;
//    private SignatureData originatorSignatureData;
//    private String signerHash;

    public MintingResponseData(MintingRequestData mintingRequestData) {
        this.hash = mintingRequestData.getHash().toString();
        this.mintingFeeWarrantHash = mintingRequestData.getMintingFeeWarrantHash().toString();
        this.mintingRequestTime = mintingRequestData.getMintingRequestTime();
        this.amount = mintingRequestData.getAmount();
        this.receiverAddress = mintingRequestData.getReceiverAddress().toString();
        this.tokenServiceFeeData = new TokenServiceFeeResponseData(mintingRequestData.getTokenServiceFeeData());
//        this.originatorSignatureData = mintingRequestData.getOriginatorSignatureData();
//        this.signerHash = mintingRequestData.getSignerHash().toString();
    }

}
