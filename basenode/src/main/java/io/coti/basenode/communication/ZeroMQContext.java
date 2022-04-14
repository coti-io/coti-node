package io.coti.basenode.communication;

import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicBoolean;

public class ZeroMQContext {
    private static ZMQ.Context context;
    private static final AtomicBoolean CONTEXT_TERMINATED = new AtomicBoolean(false);

    private ZeroMQContext() {
    }

    public static synchronized ZMQ.Context getZeroMQContext() {
        if (context == null) {
            context = ZMQ.context(1);
        }

        return context;
    }

    public static boolean isContextTerminated() {
        return CONTEXT_TERMINATED.get();
    }

    public static void setContextTerminated(boolean value) {
        CONTEXT_TERMINATED.set(value);
    }

    public static synchronized void terminate() {
        if (!context.isTerminated()) {
            context.term();
        }
    }
}
