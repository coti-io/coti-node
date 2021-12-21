package io.coti.financialserver.http.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenFeeBaseTransactionData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintingRequestData implements IEntity, ISignable, ISignValidatable {

    private static final long serialVersionUID = 3621567796701993855L;
    @NotNull
    private Hash hash;
    private Hash mintingFeeWarrantHash;
    @NotNull
    private Instant mintingRequestTime;
    @Positive
    private BigDecimal amount;
    @NotNull
    private Hash receiverAddress;
    @NotNull
    private @Valid TokenFeeBaseTransactionData tokenFeeBaseTransactionData;
    @NotNull
    private @Valid SignatureData originatorSignatureData;
    @NotNull
    private @Valid Hash signerHash;

    public MintingRequestData(Hash mintingFeeWarrantHash, Instant mintingRequestTime, BigDecimal amount, Hash receiverAddress, TokenFeeBaseTransactionData tokenFeeBaseTransactionData) {
        if (mintingFeeWarrantHash != null) {
            this.hash = CryptoHelper.cryptoHash(ArrayUtils.addAll(mintingFeeWarrantHash.getBytes(), mintingRequestTime.toString().getBytes()));
        } else {
            this.hash = CryptoHelper.cryptoHash(ArrayUtils.addAll("null".getBytes(), mintingRequestTime.toString().getBytes()));
        }
        this.mintingFeeWarrantHash = mintingFeeWarrantHash;
        this.mintingRequestTime = mintingRequestTime;
        this.amount = amount;
        this.receiverAddress = receiverAddress;
        this.tokenFeeBaseTransactionData = tokenFeeBaseTransactionData;
    }

    @Override
    public SignatureData getSignature() {
        return this.originatorSignatureData;
    }

    @Override
    public Hash getSignerHash() {
        return this.signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.originatorSignatureData = signature;
    }

}
