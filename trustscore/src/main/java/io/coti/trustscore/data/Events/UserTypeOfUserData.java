package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.UserType;
import lombok.Data;

@Data
public class UserTypeOfUserData implements IEntity {
    private Hash userHash;
    private UserType userType;

    public UserTypeOfUserData(Hash userHash, UserType userType) {
        this.userHash = userHash;
        this.userType = userType;
    }

    public Hash getHash() {
        return this.userHash;
    }

    public void setHash(Hash userHash) {
        this.userHash = userHash;
    }
}
