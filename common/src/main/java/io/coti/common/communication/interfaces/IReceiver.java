package io.coti.common.communication.interfaces;

import java.util.Map;
import java.util.function.Function;

public interface IReceiver {
    void init(Map<String, Function<Object, String>> classNameToHandlerMapping);
}
