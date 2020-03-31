package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ITokenServiceData;
import lombok.Data;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
        byte[] bytesOfOriginatorCurrencyData = originatorCurrencyData.getMessageInBytes();
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(originatorCurrencyData.getSymbol(), currencyTypeData);
        byte[] bytesOfCurrencyTypeData = currencyTypeRegistrationData.getMessageInBytes();
        byte[] bytesOfFeeAmount = feeAmount.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        return ByteBuffer.allocate(bytesOfOriginatorCurrencyData.length + bytesOfCurrencyTypeData.length + bytesOfFeeAmount.length)
                .put(bytesOfOriginatorCurrencyData).put(bytesOfCurrencyTypeData).put(bytesOfFeeAmount).array();
    }
}
