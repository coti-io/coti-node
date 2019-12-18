package io.coti.trustscore.utils;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.enums.UserType;

import java.util.HashMap;
import java.util.Map;

public class UserHashMapper {
    private static Map<Byte, UserType> byteToUserType = new HashMap<>();

    private UserHashMapper() {
        throw new IllegalStateException("Utility class");
    }

    static {
        byteToUserType.put((byte) 1, UserType.CONSUMER);
        byteToUserType.put((byte) 2, UserType.MERCHANT);
        byteToUserType.put((byte) 3, UserType.FULL_NODE);
    }

    public static UserType getUserType(Hash userHash) {
        return byteToUserType.get(userHash.getBytes()[0]);
    }
}
