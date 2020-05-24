package io.coti.basenode.data.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class GeneralMessagePayloadDataResolver extends TypeIdResolverBase {

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        GeneralMessageType generalMessageType = GeneralMessageType.getGeneralMessageType(suggestedType);
        if (generalMessageType == null) {
            throw new IllegalStateException("Invalid message class " + suggestedType);
        }
        return generalMessageType.toString();
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        GeneralMessageType generalMessageType = GeneralMessageType.valueOf(id);
        if (generalMessageType != null) {
            return context.constructType(generalMessageType.getMessagePayload());
        }
        throw new IllegalStateException("Invalid message name " + id);
    }
}
