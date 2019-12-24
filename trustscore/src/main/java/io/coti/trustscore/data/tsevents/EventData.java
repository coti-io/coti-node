package io.coti.trustscore.data.tsevents;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.http.SignedRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Data
public abstract class EventData implements IEntity {

    private static final long serialVersionUID = -7211998685201849018L;
    private LocalDate eventDate;
    private Hash hash;

    public EventData() {
    }

    public EventData(SignedRequest request) {
        LocalDateTime date = LocalDateTime.now(ZoneOffset.UTC);
        String dateString = date.toString();
        this.hash = new Hash(ByteBuffer.allocate(request.getUserHash().getBytes().length + dateString.getBytes().length).
                put(request.getUserHash().getBytes()).put(dateString.getBytes()).array());

        this.eventDate = date.toLocalDate();
    }

   public EventData(Hash hash) {
        LocalDateTime date = LocalDateTime.now(ZoneOffset.UTC);
        this.hash = hash;
        this.eventDate = date.toLocalDate();
    }

    @Override
    public Hash getHash() {
        return this.hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

}


