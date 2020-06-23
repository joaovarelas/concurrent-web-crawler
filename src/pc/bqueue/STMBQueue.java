package pc.bqueue;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

/**
 * STM implementation of queue
 *
 * @param <E> Type of elements.
 */
public class STMBQueue<E> implements BQueue<E> {

	private final Ref.View<Integer> size;
	private final Ref.View<Integer> head;
	private final TArray.View<E> array;

	/**
	 * Constructor.
	 * 
	 * @param capacity Initial queue capacity.
	 * @throws IllegalArgumentException if {@code capacity <= 0}
	 */
	public STMBQueue(int capacity) {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		size = STM.newRef(0);
		head = STM.newRef(0);
		array = STM.newTArray(capacity);
	}

	@Override
	public int capacity() {
		return array.length();
	}

	@Override
	public int size() {
		return size.get();
	}

	@Override
	public void add(E elem) {
		STM.atomic(() -> {
			if (size.get() == array.length()) {
				STM.retry();
			}
			array.update((head.get() + size.get()) % array.length(), elem);
			STM.increment(size, 1);
		});
	}

	@Override
	public E remove() {
		return STM.atomic(() -> {
			if (size.get() == 0)
				STM.retry();

			E elem = array.apply(head.get());
			head.set((head.get() + 1) % array.length());
			STM.increment(size, -1);
			return elem;
		});
	}

	/**
	 * Test instantiation (do not run in cooperative mode).
	 */
	public static final class Test extends BQueueTest {
		@Override
		<T> BQueue<T> createBQueue(int capacity) {
			return new STMBQueue<>(capacity);
		}
	}

}
