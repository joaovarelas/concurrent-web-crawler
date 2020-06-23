package pc.bqueue;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Exponential back-off utility class.
 *
 */
public final class Backoff {
	/**
	 * Minimum delay (1 nano-second).
	 */
	public static final int MIN_DELAY = 1;

	/**
	 * Maximum delay (1 millisecond minus 1 nanosecond).
	 */
	public static final int MAX_DELAY = 999_999; // not more than 1 millisecond

	private static ThreadLocal<Integer> TLBOUND = ThreadLocal.withInitial(() -> MIN_DELAY);

	/**
	 * Delay thread using exponential back-off.
	 */
	public static void delay() {
		Random rng = ThreadLocalRandom.current();
		try {
			int current_bound = TLBOUND.get();
			int delay = 1 + rng.nextInt(current_bound);
			Thread.sleep(0, delay);
			TLBOUND.set(Math.min(MAX_DELAY, current_bound * 2));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reset exponential back-off limit back to minimum.
	 */
	public static void reset() {
		TLBOUND.set(MIN_DELAY);
	}

	// Private constructor (prevent undesirable instantiation).
	private Backoff() {
	}
}
