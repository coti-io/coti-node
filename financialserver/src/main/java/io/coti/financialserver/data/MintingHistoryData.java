package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintingHistoryData implements IEntity {

    private static final long serialVersionUID = 4545495200393507685L;
    private Hash hash;
    private Instant mintingTime;
    protected BigDecimal mintingAmount;
    private Hash mintingTransactionHash;
    private Hash feeTransactionHash;

    public MintingHistoryData(Hash currencyHash, Instant mintingTime, BigDecimal mintingAmount, Hash mintingTransactionHash, Hash feeTransactionHash) {
        this.hash = CryptoHelper.cryptoHash(ArrayUtils.addAll(currencyHash.getBytes(), mintingTime.toString().getBytes()));
        this.mintingTime = mintingTime;
        this.mintingAmount = mintingAmount;
        this.mintingTransactionHash = mintingTransactionHash;
        this.feeTransactionHash = feeTransactionHash;
    }
}
