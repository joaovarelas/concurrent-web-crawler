package pc.bqueue;

/**
 * Interface for deques unbounded capacity.
 * 
 * @param <E> Type of elements.
 */
public interface BDeque<E> {

	/**
	 * Get the size of the deque.
	 * 
	 * @return The number of elements in the deque.
	 */
	int size();

	/**
	 * Add an element to the head of the deque.
	 * 
	 * @param elem Element to add.
	 */
	void addFirst(E elem);

	/**
	 * Remove an element from the head of the deque.
	 * 
	 * The operation MUST block the calling thread while the deque is empty before
	 * returning.
	 * 
	 * @return Element removed from the deque.
	 */
	E removeFirst();

	/**
	 * Add an element to the tail of the deque.
	 * 
	 * @param elem Element to add.
	 */
	void addLast(E elem);

	/**
	 * Remove an element from the tail of the deque.
	 * 
	 * The operation MUST block the calling thread while the deque is empty before
	 * returning.
	 * 
	 * @return Element removed from the deque.
	 */
	E removeLast();
}
