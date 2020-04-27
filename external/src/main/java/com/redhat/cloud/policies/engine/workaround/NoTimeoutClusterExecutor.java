package com.redhat.cloud.policies.engine.workaround;

import org.hawkular.alerts.engine.cache.IspnCacheManager;
import org.infinispan.manager.ClusterExecutionPolicy;
import org.infinispan.manager.ClusterExecutor;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.function.TriConsumer;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

public class NoTimeoutClusterExecutor implements ClusterExecutor {
    private ExecutorService timeoutExecutor;
    private Executor localExecutor = ForkJoinPool.commonPool();
    private EmbeddedCacheManager manager;

    public NoTimeoutClusterExecutor(ExecutorService executorService) {
        timeoutExecutor = executorService;
        manager = IspnCacheManager.getCacheManager();
    }

    @Override
    public CompletableFuture<Void> submit(Runnable runnable) {
        return null;
    }

    @Override
    public <V> CompletableFuture<Void> submitConsumer(Function<? super EmbeddedCacheManager, ? extends V> callable,
                                                      TriConsumer<? super Address, ? super V, ? super Throwable> triConsumer) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        localInvocation(callable).whenComplete((r, t) -> {
                future.complete(null);
        });
        return future;
    }

    <T> CompletableFuture<T> localInvocation(Function<? super EmbeddedCacheManager, ? extends T> function) {
        CompletableFuture<T> future = new CompletableFuture<>();
        localExecutor.execute(() -> {
            try {
                T result = function.apply(manager);
                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public ClusterExecutor timeout(long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public ClusterExecutor singleNodeSubmission() {
        return null;
    }

    @Override
    public ClusterExecutor singleNodeSubmission(int i) {
        return null;
    }

    @Override
    public ClusterExecutor allNodeSubmission() {
        return null;
    }

    @Override
    public ClusterExecutor filterTargets(Predicate<? super Address> predicate) {
        return null;
    }

    @Override
    public ClusterExecutor filterTargets(ClusterExecutionPolicy clusterExecutionPolicy) throws IllegalStateException {
        return null;
    }

    @Override
    public ClusterExecutor filterTargets(ClusterExecutionPolicy clusterExecutionPolicy, Predicate<? super Address> predicate) throws IllegalStateException {
        return null;
    }

    @Override
    public ClusterExecutor filterTargets(Collection<Address> collection) {
        return null;
    }

    @Override
    public ClusterExecutor noFilter() {
        return null;
    }
}
