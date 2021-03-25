package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionHelper;
import io.coti.fullnode.data.AddressTransactionsByAttachment;
import io.coti.fullnode.model.AddressTransactionsByAttachments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TransactionHelper extends BaseNodeTransactionHelper {

    @Autowired
    private AddressTransactionsByAttachments addressTransactionsByAttachments;

    @Override
    public void updateAddressTransactionHistory(TransactionData transactionData) {
        super.updateAddressTransactionHistory(transactionData);

        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            AddressTransactionsByAttachment addressTransactionsByAttachment = Optional.ofNullable(addressTransactionsByAttachments.getByHash(baseTransactionData.getAddressHash()))
                    .orElse(new AddressTransactionsByAttachment(baseTransactionData.getAddressHash()));
            if (!addressTransactionsByAttachment.addTransactionHashToHistory(transactionData.getHash(), transactionData.getAttachmentTime())) {
                log.debug("Transaction {} with attachment time {} is already in history of address {}", transactionData.getHash(), transactionData.getAttachmentTime(), baseTransactionData.getAddressHash());
            }
            addressTransactionsByAttachments.put(addressTransactionsByAttachment);
        });
    }

    public void updateAddressTransactionByAttachment(Map<Hash, AddressTransactionsByAttachment> addressToTransactionsByAttachmentMap, TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            AddressTransactionsByAttachment addressTransactionsByAttachment;
            if (!addressToTransactionsByAttachmentMap.containsKey(baseTransactionData.getAddressHash())) {
                addressTransactionsByAttachment = addressTransactionsByAttachments.getByHash(baseTransactionData.getAddressHash());
                if (addressTransactionsByAttachment == null) {
                    addressTransactionsByAttachment = new AddressTransactionsByAttachment(baseTransactionData.getAddressHash());
                }
            } else {
                addressTransactionsByAttachment = addressToTransactionsByAttachmentMap.get(baseTransactionData.getAddressHash());
            }

            if (!addressTransactionsByAttachment.addTransactionHashToHistory(transactionData.getHash(), transactionData.getAttachmentTime())) {
                log.debug("Transaction {} with attachment time {} is already in history of address {}", transactionData.getHash(), transactionData.getAttachmentTime(), baseTransactionData.getAddressHash());
            }
            addressToTransactionsByAttachmentMap.put(baseTransactionData.getAddressHash(), addressTransactionsByAttachment);
        });
    }

}
