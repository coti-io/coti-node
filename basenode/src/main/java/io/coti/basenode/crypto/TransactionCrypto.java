package io.coti.basenode.crypto;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Component
public class TransactionCrypto extends SignatureCrypto<TransactionData> {

    private final static int baseTransactionHashSize = 32;

    @Override
    public byte[] getSignatureMessage(TransactionData transactionData) {

        byte[] transactionHashInBytes = transactionData.getHash().getBytes();
        byte[] attachmentTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(transactionData.getAttachmentTime().toEpochMilli()).array();
        byte[] noncesInBytes = new byte[0];
        if (transactionData.getNonces() != null) {
            ByteBuffer nonceBuffer = ByteBuffer.allocate(transactionData.getNonces().length * Integer.BYTES);
            for (int nonce : transactionData.getNonces()) {
                nonceBuffer.putInt(nonce);
            }
            noncesInBytes = nonceBuffer.array();
        }
        ByteBuffer transactionBuffer = ByteBuffer.allocate(transactionHashInBytes.length + attachmentTimeInBytes.length + noncesInBytes.length)
                .put(transactionHashInBytes).put(attachmentTimeInBytes).put(noncesInBytes);
        return CryptoHelper.cryptoHash(transactionBuffer.array()).getBytes();

    }

    private byte[] getBaseTransactionsHashesBytes(TransactionData transactionData) {
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(baseTransactions.size() * baseTransactionHashSize);
        baseTransactions.forEach(baseTransaction -> {
            byte[] baseTransactionHashBytes = baseTransaction.getHash().getBytes();
            baseTransactionHashBuffer.put(baseTransactionHashBytes);
        });
        return baseTransactionHashBuffer.array();
    }

    public Hash getHashFromBaseTransactionHashesData(TransactionData transactionData) {
        byte[] bytesToHash = getBaseTransactionsHashesBytes(transactionData);
        return CryptoHelper.cryptoHash(bytesToHash);
    }


    public void setTransactionHash(TransactionData transactionData) {
        try {
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
                if (baseTransactionData.getHash() == null) {
                    BaseTransactionCrypto.valueOf(baseTransactionData.getClass().getSimpleName()).setBaseTransactionHash(baseTransactionData);
                }
            }

            Hash transactionHash = getHashFromBaseTransactionHashesData(transactionData);
            transactionData.setHash(transactionHash);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private boolean isTransactionHashCorrect(TransactionData transactionData) {

        Hash generatedTransactionHashFromBaseTransactions = getHashFromBaseTransactionHashesData(transactionData);
        Hash transactionHashFromData = transactionData.getHash();

        return generatedTransactionHashFromBaseTransactions.equals(transactionHashFromData);
    }

    public boolean isTransactionValid(TransactionData transactionData) {

        if (!this.isTransactionHashCorrect(transactionData))
            return false;
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (!BaseTransactionCrypto.valueOf(baseTransactionData.getClass().getSimpleName()).isBaseTransactionValid(transactionData, baseTransactionData))
                return false;
        }
        return true;
    }


}
