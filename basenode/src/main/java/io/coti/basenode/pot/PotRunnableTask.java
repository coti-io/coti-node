package io.coti.basenode.pot;

import io.coti.basenode.data.TransactionData;
import io.coti.pot.ProofOfTrust;

public class PotRunnableTask implements Comparable<PotRunnableTask>, Runnable {

    private byte[] targetDifficulty;
    private final TransactionData transactionData;

    public PotRunnableTask(TransactionData transactionData, byte[] targetDifficulty) {
        this.transactionData = transactionData;
        this.targetDifficulty = targetDifficulty;
    }

    public int getPriority() {
        return this.transactionData.getRoundedSenderTrustScore();
    }

    @Override
    public void run() {
        potAction(transactionData);
        synchronized (transactionData) {
            transactionData.notifyAll();
        }
    }


    public void potAction(TransactionData transactionData) {
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
}
