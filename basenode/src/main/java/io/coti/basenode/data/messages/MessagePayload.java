package io.coti.basenode.data.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
        property = "generalMessageType")
@JsonTypeIdResolver(GeneralMessagePayloadDataResolver.class)
public abstract class MessagePayload implements Serializable {

    protected GeneralMessageType generalMessageType;

    public MessagePayload() {
    }

    public MessagePayload(GeneralMessageType generalMessageType) {
        this.generalMessageType = generalMessageType;
    }

    public abstract byte[] getMessageInBytes();
}
