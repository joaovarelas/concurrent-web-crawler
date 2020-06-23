package pc.bqueue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lock-free implementation of queue - unbounded variant.
 * 
 *
 * @param <E> Type of elements.
 */
public class LFBQueueU<E> extends LFBQueue<E> {

	private final AtomicBoolean addElementFlag;

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity Initial queue capacity.
	 * @param backoff         Flag to enable/disable the use of back-off.
	 * @throws IllegalArgumentException if {@code capacity <= 0}
	 */
	public LFBQueueU(int initialCapacity, boolean backoff) {
		super(initialCapacity, backoff);
		addElementFlag = new AtomicBoolean();
	}

	@Override
	public int capacity() {
		return UNBOUNDED;
	}

	@Override
	public void add(E elem) {
		while (true) {
			rooms.enter(0);

			boolean f = addElementFlag.getAndSet(true);
			if (f) {
				rooms.leave(0);
				if (useBackoff) {
					Backoff.delay();
				}
				continue;
			}

			// queue is full
			if (tail.get() == array.length) {
				E[] newArray = Arrays.copyOf(array, 2 * array.length);
				array = newArray;
			}

			int p = tail.getAndIncrement();
			if (p - head.get() < array.length) {
				array[p % array.length] = elem;
				addElementFlag.set(false);
				rooms.leave(0);
				break;

			} else {
				// "undo"
				tail.getAndDecrement();
			}
			addElementFlag.set(false);
			rooms.leave(0);

			if (useBackoff) {
				Backoff.delay();
			}
		}
		if (useBackoff) {
			Backoff.reset();
		}
	}

	/**
	 * Test instantiation.
	 */
	public static final class Test extends BQueueTest {
		@Override
		<T> BQueue<T> createBQueue(int capacity) {
			return new LFBQueueU<>(capacity, false);
		}
	}
}
