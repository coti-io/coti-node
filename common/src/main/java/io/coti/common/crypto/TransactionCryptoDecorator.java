package io.coti.common.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TransactionCryptoDecorator {
    final static int  baseTransactionHashSize = 32;
    ArrayList<BasicTransactionCryptoDecorator> baseTransactions = new ArrayList<>();
    TransactionData txData;

    public TransactionCryptoDecorator(TransactionData txData)
    {
        this.txData = txData;
        for (BaseTransactionData bxData: txData.getBaseTransactions())
        {
            baseTransactions.add(new BasicTransactionCryptoDecorator(bxData));
        }
    }

    public TransactionCryptoDecorator(List<BaseTransactionData> basicTransaction, Hash transactionHash, String transactionDescription, double senderTransactionScore)
    {
        this(new TransactionData(basicTransaction,transactionHash,transactionDescription,senderTransactionScore));

    }



    private byte[] getBaseTransactionsHashesBytes()
    {
        ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(baseTransactions.size() * baseTransactionHashSize);
        for (BasicTransactionCryptoDecorator bxCrypto: this.baseTransactions) {
            byte[] baseTransactionHashBytes = bxCrypto.getBasicTransactionHash().getBytes();
            baseTransactionHashBuffer = baseTransactionHashBuffer.put(baseTransactionHashBytes);
        }
        return baseTransactionHashBuffer.array();
    }

    public String getHashFromBasicTransactionHashesData(){
        Keccak.Digest256 digest = new Keccak.Digest256();
        byte[] bytesToHash = getBaseTransactionsHashesBytes();
        digest.update(bytesToHash);
        byte[] digestedHash = digest.digest();
        String hash =   Hex.toHexString( digestedHash);
        return hash;
    }



    private boolean IsTransacHashCorrect()
    {

        String generatedTxHashFromBaseTransactions = getHashFromBasicTransactionHashesData();
        String txHashFromData = this.txData.getHash().toHexString();

        return generatedTxHashFromBaseTransactions.equals(txHashFromData);
    }

    public boolean isTransactionValid() {

        if (!this.IsTransacHashCorrect())
            return false;
        for (BasicTransactionCryptoDecorator bxCrypto: this.baseTransactions) {
            if (bxCrypto.IsBasicTransactionValid(txData.getHash()) ==false)
                return false;
        }
        return true;
    }


}
