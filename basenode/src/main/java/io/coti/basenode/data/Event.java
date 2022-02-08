package io.coti.basenode.data;

public enum Event {
    MULTI_CURRENCY(true);

    private final boolean hardFork;

    Event(boolean hardFork) {
        this.hardFork = hardFork;
    }

    public boolean isHardFork() {
        return hardFork;
    }
}
