package io.coti.common.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;

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
