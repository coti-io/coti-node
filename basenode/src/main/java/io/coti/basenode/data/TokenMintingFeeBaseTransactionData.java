package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenMintingFeeBaseTransactionData extends TokenFeeBaseTransactionData {

    private static final long serialVersionUID = 7641308128208586733L;
    private TokenMintingData serviceData;

    public TokenMintingFeeBaseTransactionData() {
    }

    public TokenMintingFeeBaseTransactionData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime, TokenMintingData tokenMintingData) {
        super(addressHash, currencyHash, signerHash, amount, createTime);
        this.serviceData = tokenMintingData;
    }

}
