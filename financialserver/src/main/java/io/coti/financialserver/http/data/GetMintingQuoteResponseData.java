package io.coti.financialserver.http.data;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.data.MintingFeeWarrantData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class GetMintingQuoteResponseData implements IResponse {

    private String hash;
    private Instant requestTime;
    private BigDecimal amount;
    private BigDecimal feeGiven;
//    private String signerHash;
//    private SignatureData signatureData;

    private MintingResponseData mintingRequest;

    public GetMintingQuoteResponseData(MintingFeeWarrantData mintingFeeWarrantData, MintingResponseData mintingRequest) {
        this.hash = mintingFeeWarrantData.getHash().toString();
        this.requestTime = mintingFeeWarrantData.getRequestTime();
        this.amount = mintingFeeWarrantData.getAmount();
        this.feeGiven = mintingFeeWarrantData.getFeeGiven();
//        this.signerHash = mintingFeeWarrantData.getSignerHash().toString();
//        this.signatureData = mintingFeeWarrantData.getSignatureData();
        this.mintingRequest = mintingRequest;
    }
}
