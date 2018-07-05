package io.coti.cotinode.crypto;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.*;

public class BasicTransactionCryptoDecorator {

    BaseTransactionData baseTxData;

    public BasicTransactionCryptoDecorator(BaseTransactionData baseTxData)
    {
        this.baseTxData = baseTxData;
    }

    private byte[] getBytes()
    {
        byte[] addressBytes = DatatypeConverter.parseHexBinary(baseTxData.getAddressHash().toHexString());

        ByteBuffer bufferIndex = ByteBuffer.allocate(4);
        bufferIndex.putInt(baseTxData.getIndexInTransactionsChain());
        byte[] IndexByteArray = bufferIndex.array();

        Date baseTransactionDate = baseTxData.getCreateTime();
        int interval = (int)(baseTransactionDate.getTime()/1000);
        ByteBuffer dateBuffer = ByteBuffer.allocate(4);
        byte[]  createTimeAsByteArray = dateBuffer.array();

        dateBuffer.putInt(interval);
        ByteBuffer baseTransactionArray = ByteBuffer.wrap(addressBytes);
        baseTransactionArray.put(IndexByteArray);
        baseTransactionArray.put(createTimeAsByteArray);

        return  baseTransactionArray.array();
    }

    public byte[] getOriginalMessageInByte(){
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(getBytes());
        return digest.digest();

    }

    public Hash getBasicTransactionHash()
    {
        return baseTxData.getHash();
    }


}
