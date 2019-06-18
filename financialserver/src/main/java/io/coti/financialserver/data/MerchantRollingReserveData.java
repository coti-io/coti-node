package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MerchantRollingReserveData implements IEntity, ISignable, ISignValidatable {

    private static final long serialVersionUID = -4054263333198615240L;
    private Hash merchantHash;
    private Hash rollingReserveAddress;
    private Integer addressIndex;
    private List<Date> releaseDates;
    private SignatureData merchantSignature;

    public MerchantRollingReserveData() {
        releaseDates = new ArrayList<>();
    }

    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.merchantHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return merchantSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        merchantSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return merchantHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        merchantHash = hash;
    }
}
