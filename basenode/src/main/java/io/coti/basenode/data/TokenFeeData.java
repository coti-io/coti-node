package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.services.interfaces.ITokenFeeData;
import lombok.Getter;

import static io.coti.basenode.crypto.CryptoHelper.calculateTokenFeeHash;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
public abstract class TokenFeeData implements ITokenFeeData {

    protected Hash tokenFeeDataHash;
    protected String symbol;
    protected Hash tokenHash;
    protected NodeFeeType nodeFeeType;

    TokenFeeData(String symbol, NodeFeeType nodeFeeType) {
        this.symbol = symbol;
        this.tokenHash = OriginatorCurrencyCrypto.calculateHash(this.symbol);
        this.nodeFeeType = nodeFeeType;
        setHash(calculateTokenFeeHash(this.tokenHash, this.nodeFeeType));
    }

    protected TokenFeeData() {

    }

    @Override
    public Hash getHash() {
        return tokenFeeDataHash;
    }

    @Override
    public void setHash(Hash hash) {
        tokenFeeDataHash = hash;
    }

}
