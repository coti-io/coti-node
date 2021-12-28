package io.coti.basenode.data;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
public class LockData {

    private final Map<Hash, AtomicInteger> lockHashMap = new ConcurrentHashMap<>();

    public AtomicInteger getByHash(Hash hash) {
        return lockHashMap.get(hash);
    }

    public AtomicInteger addLockToLockMap(Hash hash) {
        synchronized (this) {
            lockHashMap.computeIfPresent(hash, (lockHash, counter) -> {
                counter.incrementAndGet();
                return counter;
            });
            lockHashMap.putIfAbsent(hash, new AtomicInteger(0));
            if (hash.toString().startsWith("366"))
                log.info("366:" + lockHashMap.get(hash).toString());
            if (hash.toString().startsWith("cd5"))
                log.info("cd5:" + lockHashMap.get(hash).toString());
            return lockHashMap.get(hash);
        }
    }

    public void removeLockFromLocksMap(Hash hash) {
        synchronized (this) {
            lockHashMap.computeIfPresent(hash, (lockHash, counter) -> {
                counter.decrementAndGet();
                return counter;
            });
            AtomicInteger counter = lockHashMap.get(hash);
            if (counter != null && counter.get() == 0) {
                lockHashMap.remove(hash);
            }
        }
    }

}
