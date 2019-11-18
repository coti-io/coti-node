package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.financialserver.data.MintedTokenData;
import io.coti.financialserver.data.MintingFeeWarrantData;
import io.coti.financialserver.http.data.MintingRequestData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

@Data
public class MintingRecordData implements IEntity {

    private static final long serialVersionUID = 5889649093085123791L;
    @NotNull
    private Hash hash;
    @Positive
    private BigDecimal totalRequestedSupply;
    @Positive
    private BigDecimal tokenMintedAmount;

    private HashMap<Instant, MintedTokenData> mintingHistory;
    private HashMap<Hash, MintingFeeWarrantData> mintingFeeWarrants;
    private HashMap<Hash, MintingRequestData> mintingRequests;

    public MintingRecordData(Hash hash) {
        this.hash = hash;
        this.tokenMintedAmount = BigDecimal.ZERO;
        this.totalRequestedSupply = BigDecimal.ZERO;
        this.mintingHistory = new HashMap<>();
        this.mintingFeeWarrants = new HashMap<>();
        this.mintingRequests = new HashMap<>();
    }

}
