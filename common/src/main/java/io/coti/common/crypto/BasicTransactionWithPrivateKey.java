package io.coti.common.crypto;

import io.coti.common.crypto.Interfaces.IPrivateKey;
import io.coti.common.data.BaseTransactionData;

import java.math.BigDecimal;
import java.util.Date;

public class BasicTransactionWithPrivateKey extends BaseTransactionData implements IPrivateKey {

    private String privateKey;

    @Override
    public String getPrivateKey() {
        return privateKey;
    }


    public BasicTransactionWithPrivateKey(BigDecimal amount, Date createTime, String privateKey) {
        super(CryptoHelper.getAddressFromPrivateKey(privateKey), amount, createTime);
        this.privateKey = privateKey;
    }


}
