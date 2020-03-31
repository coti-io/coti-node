package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class CurrencyTypeRegistrationData extends CurrencyTypeData implements ISignable, ISignValidatable {

    private String symbol;

    public CurrencyTypeRegistrationData(String symbol, CurrencyTypeData currencyTypeData) {
        super(currencyTypeData);
        this.symbol = symbol;
    }

    public byte[] getMessageInBytes() {
        byte[] symbolInBytes = symbol.getBytes();
        byte[] currencyTypeInBytes = currencyType.name().getBytes();
        byte[] bytesOfCurrencyRateSourceType = currencyRateSourceType == null ? new byte[0] : currencyRateSourceType.name().getBytes();
        byte[] bytesOfRateSource = rateSource == null ? new byte[0] : rateSource.getBytes();
        byte[] bytesOfProtectionModel = protectionModel == null ? new byte[0] : protectionModel.getBytes();
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(createTime.toEpochMilli()).array();

        return ByteBuffer.allocate(symbolInBytes.length + currencyTypeInBytes.length
                + bytesOfCurrencyRateSourceType.length + bytesOfRateSource.length + bytesOfProtectionModel.length + createTimeInBytes.length)
                .put(symbolInBytes).put(currencyTypeInBytes).put(bytesOfCurrencyRateSourceType).put(bytesOfRateSource).put(bytesOfProtectionModel).put(createTimeInBytes).array();
    }

}
