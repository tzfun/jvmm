package org.beifengtz.jvmm.common.util.meta;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * description: TODO
 * date: 11:59 2023/4/6
 *
 * @author beifengtz
 */
public class ListenableFuture<R> implements Future<R> {

    private volatile R r;
    private volatile Throwable t;
    private final Object lock = new Object();
    private volatile boolean canceled;
    private volatile Listener<R> listener;
    private volatile boolean mayInterruptIfRunning = false;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        canceled = true;
        this.mayInterruptIfRunning = mayInterruptIfRunning;
        synchronized (lock) {
            lock.notifyAll();
        }
        listener = null;
        return false;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public boolean isDone() {
        return r != null || t != null;
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        if (isDone()) {
            return r;
        }
        synchronized (lock) {
            while (!isDone()) {
                lock.wait();
                if (isCancelled()) {
                    if (mayInterruptIfRunning) {
                        throw new InterruptedException();
                    } else {
                        break;
                    }
                }
            }
            return r;
        }
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isDone()) {
            return r;
        }
        synchronized (lock) {
            while (!isDone()) {
                lock.wait(unit.toMillis(timeout));
                if (isCancelled()) {
                    if (mayInterruptIfRunning) {
                        throw new InterruptedException();
                    } else {
                        break;
                    }
                }
            }
            return r;
        }
    }

    public void complete(R r) {
        if (isCancelled()) {
            return;
        }
        synchronized (lock) {
            if (isCancelled()) {
                return;
            }
            this.r = r;
            lock.notifyAll();
        }
        if (listener != null) {
            listener.onComplete(this);
        }
    }

    public R getNow(){
        return r;
    }

    public boolean isSuccess() {
        return t == null;
    }

    public void cause(Throwable t) {
        if (isCancelled()) {
            return;
        }
        synchronized (lock) {
            if (isCancelled()) {
                return;
            }
            this.t = t;
            lock.notifyAll();
        }
        if (listener != null) {
            listener.onComplete(this);
        }
    }

    public Throwable getCause() {
        return t;
    }

    public void registerListener(Listener<R> listener) {
        this.listener = listener;
    }

    public interface Listener<T> {
        void onComplete(ListenableFuture<T> f);
    }
}
