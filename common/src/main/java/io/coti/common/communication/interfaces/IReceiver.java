package io.coti.common.communication.interfaces;

import java.util.HashMap;
import java.util.function.Function;

public interface IReceiver {
    void init(HashMap<String, Function<Object, String>> classNameToHandlerMapping);
}
