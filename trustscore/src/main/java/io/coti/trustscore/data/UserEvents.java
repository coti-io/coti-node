package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.EventData;
import lombok.Data;
import java.util.HashMap;

@Data
public class UserEvents implements IEntity {

    public Hash userHash;
    public HashMap<EventType, HashMap<Hash, EventData>> events;

    public UserEvents(Hash userHash){
        this.userHash = userHash;


        for (EventType eventType : EventType.values())
            events.put(eventType, new HashMap<>());
    }

    public boolean eventExists(EventData event){
        if (events.containsKey(event.getEventType()))
            return events.get(event.getEventType()).containsKey(event.getHash());
        return false;
    }

    public void addEvent(EventData event){
        if (!eventExists(event))
            events.get(event.getEventType()).put(event.getHash(), event);
    }

    @Override
    public Hash getHash() {
        return this.userHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.userHash = hash;
    }
}



