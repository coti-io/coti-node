package io.coti.basenode.http.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class BaseTransactionResponseDataResolver extends TypeIdResolverBase {

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        BaseTransactionResponseClass baseTransactionResponseClass = BaseTransactionResponseClass.getName(suggestedType);
        if (baseTransactionResponseClass == null) {
            throw new IllegalStateException("Invalid base transaction class " + suggestedType);
        }
        return baseTransactionResponseClass.name();
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        try {
            BaseTransactionResponseClass baseTransactionResponse = BaseTransactionResponseClass.valueOf(id);
            return context.constructType(baseTransactionResponse.getResponseClass());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid base transaction name " + id, e);
        }
    }
}
