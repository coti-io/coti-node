package io.coti.zerospend.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.Interfaces.IPrivateKey;
import io.coti.basenode.data.BaseTransactionData;

import java.math.BigDecimal;
import java.util.Date;

public class BaseTransactionWithPrivateKey extends BaseTransactionData implements IPrivateKey {

    private String privateKey;

    @Override
    public String getPrivateKey() {
        return privateKey;
    }


    public BaseTransactionWithPrivateKey(BigDecimal amount, Date createTime, String privateKey) {
        super(CryptoHelper.getAddressFromPrivateKey(privateKey), amount, createTime);
        this.privateKey = privateKey;
    }


}
