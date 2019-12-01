package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ITokenServiceData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
        byte[] bytesOfCurrencyHash = mintingCurrencyHash.getBytes();
        byte[] bytesOfAmount = mintingAmount.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] bytesOfFeeAmount = feeAmount != null ? feeAmount.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] bytesOfReceiverAddress = receiverAddress.getBytes();

        return ByteBuffer.allocate(bytesOfCurrencyHash.length + bytesOfAmount.length + bytesOfFeeAmount.length
                + bytesOfReceiverAddress.length + Long.BYTES)
                .put(bytesOfCurrencyHash).put(bytesOfAmount).put(bytesOfFeeAmount).put(bytesOfReceiverAddress)
                .putLong(createTime.toEpochMilli()).array();
    }
}
