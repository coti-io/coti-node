package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.InputBaseTransactionData;
import io.coti.basenode.data.OutputBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TokenMintingFeeBaseTransactionData;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;

import java.time.Instant;
import java.util.Optional;

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
            received = updateForMintedAddress(received, baseTransactionData, transactionData, addressHash);
        });
    }

    private boolean updateForMintedAddress(boolean received, BaseTransactionData baseTransactionData, TransactionData transactionData, Hash addressHash) {
        if (baseTransactionData instanceof TokenMintingFeeBaseTransactionData) {
            Optional<BaseTransactionData> identicalAddresses = transactionData.getBaseTransactions().stream().filter(t -> t.getAddressHash().equals(addressHash)).findFirst();
            if (!identicalAddresses.isPresent() && ((TokenMintingFeeBaseTransactionData)baseTransactionData).getServiceData().getReceiverAddress().equals(addressHash) ) {
                return true;
            } else {
                return received;
            }
        } else {
            return received;
        }
    }
}
