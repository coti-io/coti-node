package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class BaseTransactionDataResolver extends TypeIdResolverBase {
    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        BaseTransactionName baseTransactionName = BaseTransactionName.getName(suggestedType);
        if (baseTransactionName == null) {
            throw new IllegalStateException("Invalid base transaction class " + suggestedType.getClass());
        }
        return baseTransactionName.name();
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        BaseTransactionName baseTransactionName = BaseTransactionName.valueOf(id);
        if (baseTransactionName != null) {
            return context.constructType(baseTransactionName.getBaseTransactionClass());
        }
        throw new IllegalStateException("Invalid base transaction name " + id);
    }
}
