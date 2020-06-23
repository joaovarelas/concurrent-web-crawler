package pc.bqueue;

/**
 * Blocking queue interface.
 * 
 * @param <E> Type of elements.
 */
public interface BQueue<E> {

	/**
	 * Get the size of the queue.
	 * 
	 * @return The number of elements in the queue.
	 */
	int size();

	/**
	 * Add an element to the queue.
	 * 
	 * The operation MAY block the calling thread if the queue has bounded capacity.
	 * 
	 * @param elem Element to add.
	 */
	void add(E elem);

	/**
	 * Remove an element to the queue.
	 * 
	 * The operation MUST block the calling thread while the queue is empty before
	 * returning.
	 * 
	 * @return Element removed from the queue.
	 */
	E remove();

	/**
	 * Constant (<code>-1</code>) returned by <code>capacity</code> to indicate
	 * queue has no fixed capacity.
	 */
	int UNBOUNDED = -1;

	/**
	 * Get queue capacity.
	 * 
	 * @return The fixed queue capacity, or <code>UNBOUNDED</code> is queue capacity
	 *         is not bounded.
	 */
	int capacity();

	/**
	 * Indicate if queue has bounded capacity.
	 * 
	 * @return <code>capacity() != UNBOUNDED</code>.
	 */
	default boolean hasFixedCapacity() {
		return capacity() != UNBOUNDED;
	}

}
