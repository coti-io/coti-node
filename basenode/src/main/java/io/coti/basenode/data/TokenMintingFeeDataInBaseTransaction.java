package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IServiceDataInBaseTransaction;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Data
public class TokenMintingFeeDataInBaseTransaction implements IServiceDataInBaseTransaction {

    @NotNull
    private Hash mintingCurrencyHash;
    @NotNull
    private BigDecimal mintingAmount;
    private Hash receiverAddress;
    @NotNull
    private Instant requestTime;
    @NotNull
    private BigDecimal feeQuote;

    public TokenMintingFeeDataInBaseTransaction() {
    }

    public TokenMintingFeeDataInBaseTransaction(Hash mintingCurrencyHash, BigDecimal mintingAmount, Hash receiverAddress, Instant requestTime, BigDecimal feeQuote) {
        this.mintingCurrencyHash = mintingCurrencyHash;
        this.mintingAmount = mintingAmount;
        this.receiverAddress = receiverAddress;
        this.requestTime = requestTime;
        this.feeQuote = feeQuote;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] bytesOfCurrencyHash = mintingCurrencyHash.getBytes();
        byte[] bytesOfAmount = mintingAmount.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] bytesOfReceiverAddress = receiverAddress.getBytes();
        byte[] bytesOfFeeQuote = feeQuote.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        return ByteBuffer.allocate(bytesOfCurrencyHash.length + bytesOfAmount.length
                + bytesOfReceiverAddress.length + Long.BYTES + bytesOfFeeQuote.length)
                .put(bytesOfCurrencyHash).put(bytesOfAmount).put(bytesOfReceiverAddress)
                .putLong(requestTime.toEpochMilli()).put(bytesOfFeeQuote).array();
    }
}
