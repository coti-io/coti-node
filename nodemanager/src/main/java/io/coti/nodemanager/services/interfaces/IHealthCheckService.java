package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkNodeData;

import java.util.concurrent.ThreadFactory;

public interface IHealthCheckService {

    void init();

    void shutdown();

    void initNodeMonitorThreadIfAbsent(ThreadFactory threadFactory, NetworkNodeData networkNodeData);
}
