package io.coti.basenode.crypto;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

@Slf4j
@Data
public class BaseTransactionCryptoWrapper {

    private BaseTransactionData baseTransactionData;
    private static CryptoHelper cryptoHelper = new CryptoHelper();

    public BaseTransactionCryptoWrapper(BaseTransactionData baseTransactionData) {
        this.baseTransactionData = baseTransactionData;
    }


    public byte[] getMessageInBytes() {
        byte[] addressBytes = baseTransactionData.getAddressHash().getBytes();
        String decimalStringRepresentation = baseTransactionData.getAmount().toString();
        byte[] bytesOfAmount = decimalStringRepresentation.getBytes(StandardCharsets.UTF_8);

        Date baseTransactionDate = baseTransactionData.getCreateTime();
        int timestamp = (int) (baseTransactionDate.getTime());

        ByteBuffer dateBuffer = ByteBuffer.allocate(4);
        dateBuffer.putInt(timestamp);

        ByteBuffer baseTransactionArray = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + dateBuffer.array().length).
                put(addressBytes).put(bytesOfAmount).put(dateBuffer.array());

        byte[] arrToReturn = baseTransactionArray.array();
        return arrToReturn;
    }

    public Hash createBaseTransactionHashFromData() {
        byte[] bytesToHash = getMessageInBytes();
        return CryptoHelper.cryptoHash(bytesToHash);
    }


    public void setBaseTransactionHash() {
        baseTransactionData.setHash(createBaseTransactionHashFromData());
    }

    public Hash getBaseTransactionHash() {
        return baseTransactionData.getHash();
    }


    public boolean IsBaseTransactionValid(Hash transactionHash) {
        try {
            if (!this.createBaseTransactionHashFromData().equals(getBaseTransactionHash()))
                return false;

            if (!CryptoHelper.IsAddressValid(baseTransactionData.getAddressHash()))
                return false;


            if (baseTransactionData.getAmount().signum() > 0)
                return true;


            String addressWithoutCRC = baseTransactionData.getAddressHash().toString().substring(0, 128);
            boolean checkSigning = cryptoHelper.VerifyByPublicKey(transactionHash.getBytes(), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), addressWithoutCRC);
            return checkSigning;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("error", e);
            return false;

        }
    }


}
