package unitTest.crypto;

import unitTest.crypto.Interfaces.IPrivateKey;
import io.coti.common.data.BaseTransactionData;

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
