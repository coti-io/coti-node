package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ITokenServiceData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenMintingData implements ITokenServiceData, ISignValidatable {

    @NotNull
    private Hash mintingCurrencyHash;
    @NotNull
    private BigDecimal mintingAmount;
    @NotNull
    private @Valid Hash receiverAddress;
    @NotNull
    private Instant createTime;
    private BigDecimal feeAmount;
    @NotNull
    private @Valid Hash signerHash;
    @NotNull
    private @Valid SignatureData signature;

    private TokenMintingData() {
    }

    @Override
    public byte[] getMessageInBytes() {
        return signature.getR().concat(signature.getS()).getBytes();
    }
}
