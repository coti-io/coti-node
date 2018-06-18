package io.coti.cotinode.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
public class Hash implements Serializable {
    private byte[] bytes;

    public Hash(){

    }

    public Hash(String hash){
        this.bytes = hash.getBytes();
    }

    public Hash(byte[] bytes){
        this.bytes = bytes;
    }

    public Hash(String hashText){
        this.bytes = hashText.getBytes();
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
    public String toString(){
        return new String(bytes);
    }

    @Override
    public String toString(){
        return new String(bytes);
    }


}
