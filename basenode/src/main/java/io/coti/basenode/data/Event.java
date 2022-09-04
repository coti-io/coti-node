package io.coti.basenode.data;

public enum Event {
    MULTI_DAG(true),
    TRUST_SCORE_CONSENSUS(true);

    private final boolean hardFork;

    Event(boolean hardFork) {
        this.hardFork = hardFork;
    }

    public boolean isHardFork() {
        return hardFork;
    }
}
