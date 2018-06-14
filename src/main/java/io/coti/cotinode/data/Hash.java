package io.coti.cotinode.data;

import java.util.Arrays;

public class Hash {
    public byte[] hash;

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Hash)) {
            return false;
        }
        return Arrays.equals(hash, ((Hash) other).hash);
    }
}
