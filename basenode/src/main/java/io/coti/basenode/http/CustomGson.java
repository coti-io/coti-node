package io.coti.basenode.http;

import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.Instant;

public class CustomGson {

    public CustomGson() {
    }

    public Gson getInstance() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (instant, type, jsonSerializationContext) ->
                new JsonPrimitive(DecimalUtils.toBigDecimal(instant.getEpochSecond(), instant.getNano()).stripTrailingZeros())
        );
        gsonBuilder.serializeNulls();
        return gsonBuilder.create();
    }
}
