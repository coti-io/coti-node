package io.coti.common.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import org.bouncycastle.util.encoders.Hex;


public class BasicTransactionCryptoDecorator {

    BaseTransactionData baseTxData;
    private static CryptoHelper crtpyoHelper = new CryptoHelper();

    public BasicTransactionCryptoDecorator(BaseTransactionData baseTxData)
    {
        this.baseTxData = baseTxData;
    }

    private byte[] getMessageWithTransactionHashInBytes(Hash transactionHash)
    {

        byte[] basicTransactionBytes = this.getMessageInBytes();

        ByteBuffer dateBuffer = ByteBuffer.allocate(basicTransactionBytes.length + transactionHash.getBytes().length);
        dateBuffer.put(basicTransactionBytes).put(transactionHash.getBytes());

        byte[] arrToReturn = dateBuffer.array();
        return  arrToReturn;
    }

    public byte[] getMessageInBytes()
    {
        byte[] addressBytes = DatatypeConverter.parseHexBinary(baseTxData.getAddressHash().toHexString());

        String decimalStringRepresentation = DatatypeConverter.printDecimal(baseTxData.getAmount());
        byte[] bytesOfAmount = decimalStringRepresentation.getBytes(StandardCharsets.UTF_8);


        ByteBuffer bufferIndex = ByteBuffer.allocate(4);
        bufferIndex.putInt(baseTxData.getIndexInTransactionsChain());
        byte[] IndexByteArray = bufferIndex.array();

        Date baseTransactionDate = baseTxData.getCreateTime();
        int interval = (int)(baseTransactionDate.getTime());

        ByteBuffer dateBuffer = ByteBuffer.allocate(4);
        dateBuffer.putInt(interval);


        ByteBuffer baseTransactionArray = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + IndexByteArray.length + dateBuffer.array().length).
                put(addressBytes).put(IndexByteArray).put(bytesOfAmount).put(dateBuffer.array());

        byte[] arrToReturn = baseTransactionArray.array();
        return  arrToReturn;
    }

    public Hash createBasicTransactionHashFromData(){
        Keccak.Digest256 digest = new Keccak.Digest256();
        byte[] bytesToHash = getMessageInBytes();
        digest.update(bytesToHash);
        Hash hash = new Hash(Hex.toHexString( digest.digest()).toUpperCase());
        return hash;
    }

    public Hash getBasicTransactionHash()
    {
        return baseTxData.getHash();
    }


    public boolean IsBasicTransactionValid(Hash transactionHash) {
        try {
            if (!this.createBasicTransactionHashFromData().equals(getBasicTransactionHash()))
                return false;


            if (!baseTxData.isSignatureExists())
                return true;

            String addressWithoutCRC = baseTxData.getAddressHash().toString().substring(0, 128);
            boolean checkSigning = crtpyoHelper.VerifyByPublicKey(getMessageWithTransactionHashInBytes(transactionHash), baseTxData.getSignatureData().getR(), baseTxData.getSignatureData().getS(), addressWithoutCRC);
            return checkSigning;

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
            e.printStackTrace();
            return false;

        }
    }


}
