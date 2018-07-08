package io.coti.cotinode.crypto;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.TransactionData;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class TransactionCryptoDecorator {
     final static int  baseTransactionHashSize = 32;
    ArrayList<BasicTransactionCryptoDecorator> baseTransactions = new ArrayList<BasicTransactionCryptoDecorator>();
    TransactionData txData;

    public TransactionCryptoDecorator(TransactionData txData)
    {
        this.txData = txData;
        for (BaseTransactionData bxData: txData.getBaseTransactions())
        {
            baseTransactions.add(new BasicTransactionCryptoDecorator(bxData));
        }
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
        String hash =   Hex.toHexString( digestedHash).toUpperCase();
        return hash;
    }



    private boolean IsTransacHashCorrect()
    {

        String generatedTxHashFromBaseTransactions = getHashFromBasicTransactionHashesData().toUpperCase();
        String txHashFromData = this.txData.getHash().toHexString().toUpperCase();

        return generatedTxHashFromBaseTransactions.equals(txHashFromData);
    }

    public boolean isTransactionValid() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        for (BasicTransactionCryptoDecorator bxCrypto: this.baseTransactions) {
            if (bxCrypto.IsBasicTransactionValid() ==false)
                    return false;
        }
        return this.IsTransacHashCorrect();
    }


}
