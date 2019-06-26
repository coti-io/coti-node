package io.coti.historynode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.ReceiverBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.data.AddressTransactionsByDate;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
@Service
public class TransactionIndexingService {

    @Autowired
    private AddressTransactionsByDates addressTransactionsByDates;
    @Autowired
    private AddressTransactionsByAddresses addressTransactionsByAddresses;



    public void addToHistoryTransactionIndexes(TransactionData transactionData) {
        Instant attachmentTime = transactionData.getAttachmentTime();
        Hash hashByDate = calculateHashByAttachmentTime(attachmentTime);
        HashSet<Hash> relatedTransactionAddressHashes = getRelatedTransactionHashes(transactionData);

        AddressTransactionsByDate transactionsByDateHash = addressTransactionsByDates.getByHash(hashByDate);
        if(transactionsByDateHash== null) {
            addressTransactionsByDates.put(new AddressTransactionsByDate(attachmentTime, relatedTransactionAddressHashes));
        } else {
            transactionsByDateHash.getAddresses().addAll(relatedTransactionAddressHashes);
            addressTransactionsByDates.put(transactionsByDateHash);
        }


        for(Hash transactionAddressHash : relatedTransactionAddressHashes) {
            HashMap<Hash, HashSet<Hash>> transactionHashesMap = new HashMap<>();
            AddressTransactionsByAddress transactionsByAddressHash = addressTransactionsByAddresses.getByHash(transactionAddressHash);
            if(transactionsByAddressHash==null) {
                HashSet<Hash> transactionHashes = new HashSet<>();
                if( !transactionHashes.add(transactionData.getHash()) ) {
                    log.info("{} was already present by address", transactionData);
                }
                transactionHashesMap.put(hashByDate, transactionHashes);
                addressTransactionsByAddresses.put(new AddressTransactionsByAddress(transactionAddressHash, transactionHashesMap));
            } else {
                HashSet<Hash> transactionsHashesByDate = transactionsByAddressHash.getTransactionHashesByDates().get(hashByDate);
                if(transactionsHashesByDate == null) {
                    HashSet<Hash> transactionsHashes = new HashSet<>();
                    transactionsHashes.add(transactionData.getHash());
                    transactionsByAddressHash.getTransactionHashesByDates().put(hashByDate, transactionsHashes);
                } else {
                    transactionsHashesByDate.add(transactionData.getHash());
                }
                addressTransactionsByAddresses.put(transactionsByAddressHash);
            }
        }
    }

    public HashSet<Hash> getRelatedTransactionHashes(TransactionData transactionData) {
        HashSet<Hash> hashes = new HashSet<>();
        hashes.add(transactionData.getSenderHash());
        for(BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if(baseTransactionData instanceof ReceiverBaseTransactionData) {
                hashes.add(baseTransactionData.getAddressHash());
            }
        }
        return hashes;
    }

    protected Hash calculateHashByAttachmentTime(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        LocalDate localDate = LocalDate.of(ldt.getYear(), ldt.getMonth(),ldt.getDayOfMonth());
        return CryptoHelper.cryptoHash(localDate.atStartOfDay().toString().getBytes());
    }
}
