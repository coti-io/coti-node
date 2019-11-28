package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ITokenServiceData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Data
public class TokenGenerationData implements ITokenServiceData {

    @NotNull
    private String name;
    @NotNull
    private String symbol;
    @NotNull
    private Hash generatingCurrencyHash;
    @NotNull
    private BigDecimal totalSupply;
    @NotNull
    private int scale;
    @NotNull
    private Instant createTime;
    @NotNull
    private BigDecimal feeAmount;

    private TokenGenerationData() {
    }

    public TokenGenerationData(String name, String symbol, Hash generatingCurrencyHash, BigDecimal totalSupply, int scale, Instant createTime, BigDecimal feeAmount) {
        this.name = name;
        this.symbol = symbol;
        this.generatingCurrencyHash = generatingCurrencyHash;
        this.totalSupply = totalSupply;
        this.scale = scale;
        this.createTime = createTime;
        this.feeAmount = feeAmount;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] bytesOfName = name.getBytes();
        byte[] bytesOfSymbol = symbol.getBytes();
        byte[] bytesOfCurrencyHash = generatingCurrencyHash.getBytes();
        byte[] bytesOfTotalSupply = totalSupply.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] bytesOfFeeQuote = feeAmount.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        return ByteBuffer.allocate(bytesOfName.length + bytesOfSymbol.length + bytesOfCurrencyHash.length + bytesOfTotalSupply.length +
                +Integer.BYTES + Long.BYTES + bytesOfFeeQuote.length)
                .put(bytesOfName).put(bytesOfSymbol).put(bytesOfCurrencyHash).put(bytesOfTotalSupply)
                .putInt(scale).putLong(createTime.toEpochMilli()).put(bytesOfFeeQuote).array();
    }
}
