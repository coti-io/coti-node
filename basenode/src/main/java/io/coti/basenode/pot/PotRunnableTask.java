package io.coti.basenode.pot;

import io.coti.basenode.data.TransactionData;
import io.coti.pot.ProofOfTrust;

import java.util.concurrent.atomic.AtomicInteger;

public class PotRunnableTask implements Comparable<PotRunnableTask>, Runnable {

    private byte[] targetDifficulty;
    private final TransactionData transactionData;
    private final AtomicInteger lock;

    public PotRunnableTask(TransactionData transactionData, byte[] targetDifficulty, AtomicInteger lock) {
        this.transactionData = transactionData;
        this.targetDifficulty = targetDifficulty;
        this.lock = lock;
    }

    public int getPriority() {
        return this.transactionData.getRoundedSenderTrustScore();
    }

    @Override
    public void run() {
        potAction(transactionData);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void potAction(TransactionData transactionData) {
        ProofOfTrust pot = new ProofOfTrust(transactionData.getRoundedSenderTrustScore());
        int[] nonces = pot.hash(transactionData.getHash().getBytes(), this.targetDifficulty);
        transactionData.setNonces(nonces);
    }

    @Override
    public int compareTo(PotRunnableTask other) {
        return Double.compare(this.getPriority(), other.getPriority());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof PotRunnableTask)) {
            return false;
        }
        return this.getPriority() == ((PotRunnableTask) o).getPriority();
    }

    @Override
    public int hashCode() {
        return this.getPriority();
    }
}
