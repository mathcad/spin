package org.spin.core.concurrent;

import org.spin.core.Assert;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Utilities for treating interruptible operations as uninterruptible. In all cases, if a thread is
 * interrupted during such a call, the call continues to block until the result is available or the
 * timeout elapses, and only then re-interrupts the thread.
 *
 * @author Anthony Zana
 * @since 10.0
 */
public final class Uninterruptibles {

    // Implementation Note: As of 3-7-11, the logic for each blocking/timeout
    // methods is identical, save for method being invoked.

    /**
     * Invokes {@code latch.}{@link CountDownLatch#await() await()} uninterruptibly.
     *
     * @param latch countdownLatch
     */
    public static void awaitUninterruptibly(CountDownLatch latch) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    latch.await();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code latch.}{@link CountDownLatch#await(long, TimeUnit) await(timeout, unit)}
     * uninterruptibly.
     *
     * @param latch   latch
     * @param timeout timeout
     * @param unit    timeout unit
     * @return false if the waiting time detectably elapsed before return from the method, else true
     */
    public static boolean awaitUninterruptibly(CountDownLatch latch, long timeout, TimeUnit unit) {
        return awaitUninterruptibly(latch::await, timeout, unit);
    }

    /**
     * Invokes {@code condition.}{@link Condition#await(long, TimeUnit) await(timeout, unit)}
     * uninterruptibly.
     *
     * @param condition condition
     * @param timeout   timeout
     * @param unit      timeout unit
     * @return false if the waiting time detectably elapsed before return from the method, else true
     * @since 23.6
     */
    public static boolean awaitUninterruptibly(Condition condition, long timeout, TimeUnit unit) {
        return awaitUninterruptibly(condition::await, timeout, unit);
    }

    /**
     * Invokes {@code toJoin.}{@link Thread#join() join()} uninterruptibly.
     *
     * @param toJoin Thread to join
     */
    public static void joinUninterruptibly(Thread toJoin) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    toJoin.join();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code unit.}{@link TimeUnit#timedJoin(Thread, long) timedJoin(toJoin, timeout)}
     * uninterruptibly.
     *
     * @param toJoin  join Thread
     * @param timeout timeout
     * @param unit    timeout unit
     */
    public static void joinUninterruptibly(Thread toJoin, long timeout, TimeUnit unit) {
        Assert.notNull(toJoin);
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(timeout);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    // TimeUnit.timedJoin() treats negative timeouts just like zero.
                    NANOSECONDS.timedJoin(toJoin, remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code future.}{@link Future#get() get()} uninterruptibly.
     *
     * <p>Similar methods:
     *
     * <ul>
     * <li>To retrieve a result from a {@code Future} that is already done.</li>
     * <li>To treat {@link InterruptedException} uniformly with other exceptions.</li>
     * <li>To get uninterruptibility and remove checked exceptions.</li>
     * </ul>
     *
     * @param <V>    generic type
     * @param future work
     * @return result
     * @throws ExecutionException    if the computation threw an exception
     * @throws CancellationException if the computation was cancelled
     */
    public static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code future.}{@link Future#get(long, TimeUnit) get(timeout, unit)} uninterruptibly.
     *
     * <p>Similar methods:
     *
     * <ul>
     * <li>To retrieve a result from a {@code Future} that is already done.
     * <li>To treat {@link InterruptedException} uniformly with other exceptions.</li>
     * <li>To get uninterruptibility and remove checked exceptions.</li>
     * </ul>
     *
     * @param <V>     generic type
     * @param future  future
     * @param timeout timeout
     * @param unit    timeout unit
     * @return result
     * @throws ExecutionException    if the computation threw an exception
     * @throws CancellationException if the computation was cancelled
     * @throws TimeoutException      if the wait timed out
     */
    public static <V> V getUninterruptibly(Future<V> future, long timeout, TimeUnit unit)
        throws ExecutionException, TimeoutException {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(timeout);
            long end = System.nanoTime() + remainingNanos;

            while (true) {
                try {
                    // Future treats negative timeouts just like zero.
                    return future.get(remainingNanos, NANOSECONDS);
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code queue.}{@link BlockingQueue#take() take()} uninterruptibly.
     *
     * @param queue queue
     * @param <E>   queue element type
     * @return element
     */
    public static <E> E takeUninterruptibly(BlockingQueue<E> queue) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return queue.take();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code queue.}{@link BlockingQueue#put(Object) put(element)} uninterruptibly.
     *
     * @param queue   queue
     * @param element element
     * @param <E>     element type
     * @throws ClassCastException       if the class of the specified element prevents it from being added
     *                                  to the given queue
     * @throws IllegalArgumentException if some property of the specified element prevents it from
     *                                  being added to the given queue
     */
    public static <E> void putUninterruptibly(BlockingQueue<E> queue, E element) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    queue.put(element);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // TODO(user): Support Sleeper somehow (wrapper or interface method)?

    /**
     * Invokes {@code unit.}{@link TimeUnit#sleep(long) sleep(sleepFor)} uninterruptibly.
     *
     * @param sleepFor sleep time
     * @param unit     time unit
     */
    public static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(sleepFor);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    // TimeUnit.sleep() treats negative timeouts just like zero.
                    NANOSECONDS.sleep(remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invokes {@code semaphore.}{@link Semaphore#tryAcquire(int, long, TimeUnit) tryAcquire(1,
     * timeout, unit)} uninterruptibly.
     *
     * @param semaphore semaphore
     * @param timeout   timeout
     * @param unit      timeout unit
     * @return true if all permits were acquired and false if the waiting time elapsed before all permits were acquired
     * @since 18.0
     */
    public static boolean tryAcquireUninterruptibly(Semaphore semaphore, long timeout, TimeUnit unit) {
        return tryAcquireUninterruptibly(semaphore, 1, timeout, unit);
    }

    /**
     * Invokes {@code semaphore.}{@link Semaphore#tryAcquire(int, long, TimeUnit) tryAcquire(permits,
     * timeout, unit)} uninterruptibly.
     *
     * @param semaphore semaphore
     * @param permits   permits
     * @param timeout   timeout
     * @param unit      timeout unit
     * @return true if all permits were acquired and false if the waiting time elapsed before all permits were acquired
     * @since 18.0
     */
    public static boolean tryAcquireUninterruptibly(Semaphore semaphore, int permits, long timeout, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(timeout);
            long end = System.nanoTime() + remainingNanos;

            while (true) {
                try {
                    // Semaphore treats negative timeouts just like zero.
                    return semaphore.tryAcquire(permits, remainingNanos, NANOSECONDS);
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // TODO(user): Add support for waitUninterruptibly.

    private Uninterruptibles() {
    }

    private static boolean awaitUninterruptibly(Latch latch, long timeout, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(timeout);
            long end = System.nanoTime() + remainingNanos;

            while (true) {
                try {
                    return latch.await(remainingNanos, NANOSECONDS);
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @FunctionalInterface
    private interface Latch {
        boolean await(long time, TimeUnit unit) throws InterruptedException;
    }
}
