package io.coti.zerospend.services.helper;

import io.coti.common.data.Hash;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class TransactionHashWrapper {
    public boolean isVoting;
    private Hash hash;

    public TransactionHashWrapper(Hash hash) {
        this.hash = hash;
    }

    public void checkSync() {
        synchronized (this) {
            while (isVoting) {
                log.info("TransactionHashWrapper is waiting on the transaction {}", hash);
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    log.error("Error - wait() call in TransactionHashWrapper", e);
                }
            }
            isVoting = true;
        }
    }


}
