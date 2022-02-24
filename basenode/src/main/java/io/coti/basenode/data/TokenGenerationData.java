package io.coti.basenode.data;

import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.interfaces.ITokenServiceData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Data
public class TokenGenerationData implements ITokenServiceData {
    @NotNull
    private @Valid OriginatorCurrencyData originatorCurrencyData;
    @NotNull
    private @Valid CurrencyTypeData currencyTypeData;
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
        byte[] bytesOfOriginatorCurrencyData = OriginatorCurrencyCrypto.getMessageInBytes(originatorCurrencyData);
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(originatorCurrencyData.getSymbol(), currencyTypeData);
        byte[] bytesOfCurrencyTypeData = CurrencyTypeRegistrationCrypto.getMessageInBytes(currencyTypeRegistrationData);
        byte[] bytesOfFeeAmount = feeAmount.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        return ByteBuffer.allocate(bytesOfOriginatorCurrencyData.length + bytesOfCurrencyTypeData.length + bytesOfFeeAmount.length)
                .put(bytesOfOriginatorCurrencyData).put(bytesOfCurrencyTypeData).put(bytesOfFeeAmount).array();
    }
}
