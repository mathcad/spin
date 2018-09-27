package org.spin.core.base;

import org.spin.core.Assert;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * An object that measures elapsed time in nanoseconds. It is useful to measure elapsed time using
 * this class instead of direct calls to {@link System#nanoTime} for a few reasons:
 *
 * <ul>
 * <li>An alternate time source can be substituted, for testing or performance reasons.
 * <li>As documented by {@code nanoTime}, the value returned has no absolute meaning, and can only
 * be interpreted as relative to another timestamp returned by {@code nanoTime} at a different
 * time. {@code Stopwatch} is a more effective abstraction because it exposes only these
 * relative values, not the absolute ones.
 * </ul>
 *
 * <p>Basic usage:
 *
 * <pre>{@code
 * Stopwatch stopwatch = Stopwatch.createStarted();
 * doSomething();
 * stopwatch.stop(); // optional
 *
 * Duration duration = stopwatch.elapsed();
 *
 * log.info("time: " + stopwatch); // formatted string like "12.3 ms"
 * }</pre>
 * pre*
 * <p>Stopwatch methods are not idempotent; it is an error to start or stop a stopwatch that is
 * already in the desired state.
 *
 * <p>When testing code that uses this class, use {@link #createUnstarted(Ticker)} or {@link
 * #createStarted(Ticker)} to supply a fake or mock ticker. This allows you to simulate any valid
 * behavior of the stopwatch.
 *
 * <p><b>Note:</b> This class is not thread-safe.
 *
 * <p><b>Warning for Android users:</b> a stopwatch with default behavior may not continue to keep
 * time while the device is asleep. Instead, create one like this:
 *
 * <pre>{@code
 * Stopwatch.createStarted(
 *      new Ticker() {
 *        public long read() {
 *          return android.os.SystemClock.elapsedRealtimeNanos();
 *        }
 *      });
 * }</pre>
 *
 * @author Kevin Bourrillion
 * @since 10.0
 */
public final class Stopwatch {
    private final Ticker ticker;
    private boolean isRunning;
    private long elapsedNanos;
    private long startTick;

    /**
     * 使用{@link System#nanoTime}作为时间源创建(但不启动)一个秒表实例
     *
     * @return 秒表实例
     */
    public static Stopwatch createUnstarted() {
        return new Stopwatch();
    }

    /**
     * 使用指定的时间源创建(但不启动)一个秒表实例
     *
     * @param ticker 时间源
     * @return 秒表实例
     */
    public static Stopwatch createUnstarted(Ticker ticker) {
        return new Stopwatch(ticker);
    }

    /**
     * 使用{@link System#nanoTime}作为时间源创建一个秒表实例，并启动计时
     *
     * @return 秒表实例
     */
    public static Stopwatch createStarted() {
        return new Stopwatch().start();
    }

    /**
     * 使用指定的时间源创建一个秒表实例，并启动计时
     *
     * @param ticker 时间源
     * @return 秒表实例
     */
    public static Stopwatch createStarted(Ticker ticker) {
        return new Stopwatch(ticker).start();
    }

    private Stopwatch() {
        this.ticker = Ticker.systemTicker();
    }

    private Stopwatch(Ticker ticker) {
        this.ticker = Assert.notNull(ticker, "ticker");
    }

    /**
     * 如果秒表正在计时(调用过{@link #start()}，并且自上次调用{@code start()}后，没有调用过{@link #stop()})，返回{@code true}
     *
     * @return 是否正在计时
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 启动当前{@code Stopwatch}实例
     *
     * @return 当前{@code Stopwatch}实例
     * @throws IllegalStateException 如果当前{@code Stopwatch}实例已经处于计时状态抛出
     */
    public Stopwatch start() {
        Assert.isTrue(!isRunning, "This stopwatch is already running.");
        isRunning = true;
        startTick = ticker.read();
        return this;
    }

    /**
     * 停止当前{@code Stopwatch}实例
     *
     * @return 当前{@code Stopwatch}实例
     * @throws IllegalStateException 如果当前{@code Stopwatch}实例已经处于停止状态时抛出
     */
    public Stopwatch stop() {
        long tick = ticker.read();
        Assert.isTrue(isRunning, "This stopwatch is already stopped.");
        isRunning = false;
        elapsedNanos += tick - startTick;
        return this;
    }

    /**
     * 重置当前{@code Stopwatch}实例，停止计时并将相关计时器归零
     *
     * @return 当前{@code Stopwatch}实例
     */
    public Stopwatch reset() {
        elapsedNanos = 0;
        isRunning = false;
        return this;
    }

    private long elapsedNanos() {
        return isRunning ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
    }

    /**
     * Returns the current elapsed time shown on this stopwatch, expressed in the desired time unit,
     * with any fraction rounded down.
     *
     * <p><b>Note:</b> the overhead of measurement can be more than a microsecond, so it is generally
     * not useful to specify {@link TimeUnit#NANOSECONDS} precision here.
     *
     * <p>It is generally not a good idea to use an ambiguous, unitless {@code long} to represent
     * elapsed time. Therefore, we recommend using {@link #elapsed()} instead, which returns a
     * strongly-typed {@link Duration} instance.
     *
     * @since 14.0 (since 10.0 as {@code elapsedTime()})
     */
    public long elapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
    }

    /**
     * Returns the current elapsed time shown on this stopwatch as a {@link Duration}. Unlike {@link
     * #elapsed(TimeUnit)}, this method does not lose any precision due to rounding.
     *
     * @since 22.0
     */
    public Duration elapsed() {
        return Duration.ofNanos(elapsedNanos());
    }

    /**
     * Returns a string representation of the current elapsed time.
     */
    @Override
    public String toString() {
        long nanos = elapsedNanos();

        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / NANOSECONDS.convert(1, unit);

        // Too bad this functionality is not exposed as a regular method call
        return String.format(Locale.ROOT, "%.4g", value) + " " + abbreviate(unit);
    }

    private static TimeUnit chooseUnit(long nanos) {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(nanos, NANOSECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }
}
