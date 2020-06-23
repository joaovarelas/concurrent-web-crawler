package pc.bqueue;

import java.util.Arrays;

/**
 * Monitor-based implementation of queue.
 * 
 *
 * @param <E> Type of elements.
 */
public class MBQueueU<E> extends MBQueue<E> {

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity Initial queue capacity.
	 * @throws IllegalArgumentException if {@code capacity <= 0}
	 */
	public MBQueueU(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public synchronized int capacity() {
		return UNBOUNDED;
	}

	@Override
	public synchronized void add(E elem) {
		synchronized (this) {
			if (size == array.length) {
				// queue is full, create new array with double size
				E[] newArray = Arrays.copyOf(array, 2 * array.length);
				array = newArray;
			}
			array[(head + size) % array.length] = elem;
			size++;
			notifyAll();
		}
	}

	/**
	 * Test instantiation.
	 */
	public static final class Test extends BQueueTest {
		@Override
		<T> BQueue<T> createBQueue(int initialCapacity) {
			return new MBQueueU<>(initialCapacity);
		}
	}
}
