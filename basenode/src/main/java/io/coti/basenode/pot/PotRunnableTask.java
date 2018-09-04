package io.coti.basenode.pot;

import io.coti.basenode.data.TransactionData;
import io.coti.pot.ProofOfTrust;

public class PotRunnableTask implements Comparable<PotRunnableTask>, Runnable {
    private byte[] targetDifficulty;
    private TransactionData transactionData;

    public int getPriority() {
        return this.transactionData.getRoundedSenderTrustScore();
    }


    public PotRunnableTask(TransactionData transactionData, byte[] targetDifficulty) {
        this.transactionData = transactionData;
        this.targetDifficulty = targetDifficulty;
    }

    @Override
    public void run() {
        potAction(transactionData);
        synchronized (transactionData) {
            transactionData.notify();
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
}
