package io.coti.trustscore.http.data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.data.BaseTransactionResponseData;
import io.coti.trustscore.data.Enums.TransactionName;

import java.math.BigDecimal;


public class FeeBaseTransactionData extends BaseTransactionResponseData {
    public SignatureData signatureData;
    public String nodeHash;
    public BigDecimal originalAmount;
    public TransactionName name;


    //This constructor is for deserialize reasons from rest api request
    protected FeeBaseTransactionData(){

    }

    public FeeBaseTransactionData(BaseTransactionData baseTransactionData, SignatureData signatureData, Hash nodeHash,
                                  BigDecimal originalAmount, TransactionName name) {
        super(baseTransactionData);
        this.signatureData = signatureData;
        this.nodeHash= nodeHash.toHexString();
        this.originalAmount = originalAmount;
        this.name = name;
    }

    @JsonIgnore
    public BaseTransactionData getBaseTransactionData(){
                return new BaseTransactionData(new Hash(addressHash),
                        amount,new Hash(hash),
                        this.signatureData,createTime);
    }
}
