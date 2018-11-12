package io.coti.trustscore.http;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.data.FeeBaseTransactionData;
import lombok.Data;


@Data
public class GetFeesBaseTransactionsResponse extends BaseResponse {

    public FeeBaseTransactionData networkBaseTransactionResponseData;
    public FeeBaseTransactionData rollingReserveBaseTransactionResponseData;
    public String nodeHash;

    public GetFeesBaseTransactionsResponse(FeeBaseTransactionData networkBaseTransactionResponseData,
                                           FeeBaseTransactionData rollingReserveBaseTransactionResponseData){
        this.nodeHash = NodeCryptoHelper.getNodeHash().toHexString();
        networkBaseTransactionResponseData.signatureData =  NodeCryptoHelper.signMessage(
                networkBaseTransactionResponseData.getHash().getBytes());
        rollingReserveBaseTransactionResponseData.signatureData =  NodeCryptoHelper.signMessage(rollingReserveBaseTransactionResponseData.getHash().getBytes());
        this.networkBaseTransactionResponseData = networkBaseTransactionResponseData;
        this.rollingReserveBaseTransactionResponseData = rollingReserveBaseTransactionResponseData;
    }

}
