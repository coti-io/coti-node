package io.coti.common.pot;


import coti.pot.ProofOfTransaction;
import io.coti.common.data.TransactionData;

public class PotRunnableTask implements Comparable<PotRunnableTask>,Runnable{
    private byte[] targetDifficulty;
    private TransactionData transactionData;

    public int getPriority(){
        return this.transactionData.getRoundedSenderTrustScore();
    }


    public PotRunnableTask(TransactionData transactionData, byte[] targetDifficulty){
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
        ProofOfTransaction pot = new ProofOfTransaction(transactionData.getRoundedSenderTrustScore());
        int[] nonces = pot.hash(transactionData.getHash().getBytes(), this.targetDifficulty);
        transactionData.setNonces(nonces);
    }



    @Override
    public int compareTo(PotRunnableTask other) {
        return Double.compare(this.getPriority(), other.getPriority());
    }
}
