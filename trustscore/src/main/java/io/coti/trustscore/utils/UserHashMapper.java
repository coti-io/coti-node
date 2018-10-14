package io.coti.trustscore.utils;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.Enums.UserType;

import java.util.HashMap;
import java.util.Map;

public class UserHashMapper {


    private static Map<Byte, UserType> byteToUserType = new HashMap<Byte, UserType>() {{
        put((byte)1,UserType.WALLET);
        put((byte)2,UserType.MERCHANT);
        put((byte)3,UserType.FULL_NODE);
    }};



    public static UserType getUserType(Hash userHash){
        return byteToUserType.get(userHash.getBytes()[0]);
    }

}
