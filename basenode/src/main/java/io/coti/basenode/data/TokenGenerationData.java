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

    private OriginatorCurrencyData originatorCurrencyData;
    private CurrencyTypeData currencyTypeData;
    private BigDecimal feeAmount;

    private TokenGenerationData() {
    }

    public TokenGenerationData(OriginatorCurrencyData originatorCurrencyData, CurrencyTypeData currencyTypeData, BigDecimal feeAmount) {
        this.originatorCurrencyData = originatorCurrencyData;
        this.currencyTypeData = currencyTypeData;
        this.feeAmount = feeAmount;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] bytesOfName = originatorCurrencyData.name.getBytes();
        byte[] bytesOfSymbol = originatorCurrencyData.symbol.getBytes();
        byte[] bytesOfCurrencyHash = originatorCurrencyData.calculateHash().getBytes();
        byte[] bytesOfTotalSupply = originatorCurrencyData.totalSupply.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] bytesOfFeeAmount = feeAmount.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        return ByteBuffer.allocate(bytesOfName.length + bytesOfSymbol.length + bytesOfCurrencyHash.length + bytesOfTotalSupply.length +
                +Integer.BYTES + Long.BYTES + bytesOfFeeAmount.length)
                .put(bytesOfName).put(bytesOfSymbol).put(bytesOfCurrencyHash).put(bytesOfTotalSupply)
                .putInt(originatorCurrencyData.scale).putLong(currencyTypeData.getCreateTime().toEpochMilli()).put(bytesOfFeeAmount).array();
    }
}
