package de.itemis.mosig.fluffyj.tests;

import static de.itemis.mosig.fluffyj.concurrency.FluffyLatches.waitOnLatch;
import static de.itemis.mosig.fluffyj.sneaky.Sneaky.throwThat;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import de.itemis.mosig.fluffyj.concurrency.FluffyFutures;
import de.itemis.mosig.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to make working with {@link Future} more straight forward.
 */
public final class InternalTestFutures {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(500);

    private InternalTestFutures() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Schedule a {@link Future} that runs forever until interrupted by a thread interrupt.
     * </p>
     * <p>
     * When this method returns, the returned {@link Future} is guaranteed to have been started.
     * </p>
     *
     * @param executor - Schedule with this {@link ExecutorService}.
     * @return A new instance of {@link Future} that does not return a value.
     */
    public static Future<?> scheduleInterruptibleFuture(ExecutorService executor) {
        requireNonNull(executor, "executor");

        CountDownLatch latch = new CountDownLatch(1);
        Future<?> result = executor.submit(() -> {
            boolean firstLoop = true;
            while (!Thread.currentThread().isInterrupted()) {
                if (firstLoop) {
                    firstLoop = false;
                    latch.countDown();
                }
            }
        });

        boolean latchWasZero = waitOnLatch(latch, Duration.ofSeconds(5));
        assertThat(latchWasZero).as("Waiting on latch to become zero timed out.").isTrue();

        return result;
    }

    /**
     * <p>
     * Schedule a {@link Future} that will throw the provided {@code expectedThrowable}, i. e. when
     * calling {@link Future#get()} the future is guaranteed to throw an {@link ExecutionException}
     * that has the provided {@code expectedThrowable} set as cause.
     * </p>
     *
     * @param executor - Schedule on this executor.
     * @param expectedThrowable - The root cause to set.
     * @return A new instance of {@link Future} that does not return a value.
     */
    public static Future<?> scheduleExceptionalFuture(ExecutorService executor, Throwable expectedThrowable) {
        requireNonNull(executor, "executor");
        requireNonNull(expectedThrowable, "expectedThrowable");

        return executor.submit(() -> {
            throwThat(expectedThrowable);
        });
    }

    /**
     * <p>
     * Schedule a {@link Future} that never stops and is not interruptible by thread interrupts.
     * </p>
     * <p>
     * Because running non interruptible code may produce resource leaks, this method will return an
     * instance of {@link NeverendingFuture} which contains a method to manually shut down the
     * future when it is not required anymore. <b>Do not forget to manually shut it down when you
     * are done!</b>
     * </p>
     *
     * @param executor - Schedule on this executor.
     * @return A new instance of {@link NeverendingFuture}.
     */
    public static NeverendingFuture scheduleNeverendingFuture(ExecutorService executor) {
        requireNonNull(executor, "executor");
        return new NeverendingFuture(executor);
    }

    /**
     * Wraps a future that does never stop running and is not interruptible by thread interrupts.
     */
    public static final class NeverendingFuture {
        private final Future<?> future;
        private final AtomicBoolean mustNotStop = new AtomicBoolean(true);

        private NeverendingFuture(ExecutorService executor) {
            this.future = executor.submit(() -> {
                while (mustNotStop.get());
            });
        }

        /**
         * @return The {@link Future} wrapped by this instance.
         */
        public Future<?> getFuture() {
            return future;
        }

        /**
         * Stop the wrapped {@link Future}. When this method returns, the {@link Future} has stopped
         * executing.
         *
         * @throws RuntimeException In case waiting on the {@link Future} to stop times out. This
         *         should never happen.
         */
        public void stop() {
            mustNotStop.set(false);
            FluffyFutures.waitOnFuture(future, DEFAULT_TIMEOUT);
        }
    }
}
