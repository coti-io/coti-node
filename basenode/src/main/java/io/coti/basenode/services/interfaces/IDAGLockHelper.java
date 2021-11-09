package io.coti.basenode.services.interfaces;

public interface IDAGLockHelper {

    void lockForRead();

    void unlockForRead();

    void lockForWrite();

    void unlockForWrite();

    boolean isWriteLockedByCurrentThread();
}
