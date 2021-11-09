package io.coti.basenode.services;

import io.coti.basenode.services.interfaces.IDAGLockHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class DAGLockHelper implements IDAGLockHelper {

    private final ReentrantReadWriteLock dagReadWriteLock = new ReentrantReadWriteLock();


    @Override
    public void lockForRead() {
        dagReadWriteLock.readLock().lock();
    }

    @Override
    public void unlockForRead() {
        dagReadWriteLock.readLock().unlock();
    }

    @Override
    public void lockForWrite() {
        dagReadWriteLock.writeLock().lock();
    }

    @Override
    public void unlockForWrite() {
        dagReadWriteLock.writeLock().unlock();
    }

    @Override
    public boolean isWriteLockedByCurrentThread() {
        return dagReadWriteLock.isWriteLockedByCurrentThread();
    }
}
