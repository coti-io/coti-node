package io.coti.basenode.http.data;

import io.coti.basenode.data.*;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;

import java.time.Instant;

@Data
public class ReducedTransactionResponseData implements ITransactionResponseData {

    private String hash;
    private Instant attachmentTime;
    private boolean sent;
    private boolean received;

    public ReducedTransactionResponseData(TransactionData transactionData, Hash addressHash) {
        hash = transactionData.getHash().toHexString();
        attachmentTime = transactionData.getAttachmentTime();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            if (baseTransactionData.getAddressHash().equals(addressHash)) {
                sent = sent || baseTransactionData instanceof InputBaseTransactionData;
                received = received || baseTransactionData instanceof OutputBaseTransactionData;
            }
            updateForMintedAddress(baseTransactionData, addressHash);
        });
    }

    private void updateForMintedAddress(BaseTransactionData baseTransactionData, Hash addressHash) {
        received = received || (baseTransactionData instanceof TokenMintingFeeBaseTransactionData && ((TokenMintingFeeBaseTransactionData) baseTransactionData).getServiceData().getReceiverAddress().equals(addressHash));
    }
}
