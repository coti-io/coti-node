package io.coti.cotinode.data;

import io.coti.cotinode.crypto.CryptoUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
public class Hash implements Serializable {
    private byte[] bytes;


    public Hash(String hash){
        this.bytes = CryptoUtils.hexStringToByteArray(hash);
    }

    public Hash(byte[] bytes){
        this.bytes = bytes;
    }

    public String toHexString(){
        return CryptoUtils.bytesToHex(bytes);
    }


    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Hash)) {
            return false;
        }
        return Arrays.equals(bytes, ((Hash) other).bytes);
    }



    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString(){
        return new String(bytes);
    }



}
