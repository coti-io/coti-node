package io.coti.common.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

@Slf4j
@Data
public class BaseTransactionCryptoWrapper {

    private BaseTransactionData baseTxData;
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

        Date baseTransactionDate = baseTxData.getCreateTime();
        int timestamp = (int)(baseTransactionDate.getTime());

        ByteBuffer dateBuffer = ByteBuffer.allocate(4);
        dateBuffer.putInt(timestamp);

        ByteBuffer baseTransactionArray = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + dateBuffer.array().length).
                put(addressBytes).put(bytesOfAmount).put(dateBuffer.array());

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


    public void setBaseTransactionHash(){
        baseTxData.setHash(createBaseTransactionHashFromData());
    }

    public Hash getBaseTransactionHash()
    {
        return baseTxData.getHash();
    }



    public static boolean IsBaseTransactionValid(Hash transactionHash, BaseTransactionData baseTransactionData) {
        try {
            if (!CryptoHelper.IsAddressValid(baseTransactionData.getAddressHash()))
                return false;

            if (baseTransactionData.getAmount().signum()>0)
                return true;

            String addressWithoutCRC = baseTransactionData.getAddressHash().toString().substring(0, 128);
            return cryptoHelper.VerifyByPublicKey(transactionHash.getBytes(), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), addressWithoutCRC);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("error", e);
            return false;
        }
    }


}
