package io.coti.common.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

@Slf4j
public class BaseTransactionCryptoWrapper {

    BaseTransactionData baseTxData;
    private static CryptoHelper cryptoHelper = new CryptoHelper();

    public BaseTransactionCryptoWrapper(BaseTransactionData baseTxData)
    {
        this.baseTxData = baseTxData;
    }

    public byte[] getMessageInBytes()
    {
        byte[] addressBytes = baseTxData.getAddressHash().getBytes();
        String decimalStringRepresentation = baseTxData.getAmount().toString();
        byte[] bytesOfAmount = decimalStringRepresentation.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bufferIndex = ByteBuffer.allocate(4);
        bufferIndex.putInt(baseTxData.getIndexInTransactionsChain());
        byte[] IndexByteArray = bufferIndex.array();

        Date baseTransactionDate = baseTxData.getCreateTime();
        int timestamp = (int)(baseTransactionDate.getTime());

        ByteBuffer dateBuffer = ByteBuffer.allocate(4);
        dateBuffer.putInt(timestamp);

        ByteBuffer baseTransactionArray = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + IndexByteArray.length + dateBuffer.array().length).
                put(addressBytes).put(IndexByteArray).put(bytesOfAmount).put(dateBuffer.array());

        byte[] arrToReturn = baseTransactionArray.array();
        return  arrToReturn;
    }

    public Hash createBaseTransactionHashFromData(){
        Keccak.Digest256 digest = new Keccak.Digest256();
        byte[] bytesToHash = getMessageInBytes();
        digest.update(bytesToHash);
        Hash hash = new Hash(Hex.toHexString(digest.digest()));
        return hash;
    }

    public Hash getBaseTransactionHash()
    {
        return baseTxData.getHash();
    }

    public boolean IsBaseTransactionValid(Hash transactionHash) {
        try {
            if (!this.createBaseTransactionHashFromData().equals(getBaseTransactionHash()))
                return false;

            if (!CryptoHelper.IsAddressValid(baseTxData.getAddressHash()))
                return false;


            if (baseTxData.getAmount().signum()>0)
                return true;


            String addressWithoutCRC = baseTxData.getAddressHash().toString().substring(0, 128);
            boolean checkSigning = cryptoHelper.VerifyByPublicKey(transactionHash.getBytes(), baseTxData.getSignatureData().getR(), baseTxData.getSignatureData().getS(), addressWithoutCRC);
            return checkSigning;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("error", e);
            return false;

        }
    }


}
