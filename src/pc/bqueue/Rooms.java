package pc.bqueue;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * Rooms utility class.
 */
public final class Rooms {

	/**
	 * Number of rooms.
	 */
	private final int numberOfRooms;

	/**
	 * Back-off flag.
	 */
	private final boolean useBackoff;

	/**
	 * Current state.
	 */
	private final AtomicReference<State> currentState = new AtomicReference<>(State.FREE);

	/**
	 * Constructor.
	 * 
	 * @param n       Number of rooms.
	 * @param backoff Enable / disable the use of back-off
	 * @throws IllegalArgumentException if {@code n < 2}.
	 */
	public Rooms(int n, boolean backoff) throws IllegalArgumentException {
		if (n < 2) {
			throw new IllegalArgumentException();
		}
		numberOfRooms = n;
		useBackoff = backoff;
	}

	/**
	 * Get number of rooms.
	 * 
	 * @return The number of rooms.
	 */
	public int count() {
		return numberOfRooms;
	}

	/**
	 * Enter room.
	 * 
	 * @param r Room number.
	 */
	public void enter(int r) {
		if (r < 0 || r >= numberOfRooms) {
			throw new IllegalArgumentException();
		}

		changeState(s -> s.onEnter(r));

		if (currentState.get().room != r) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Leave room.
	 * 
	 * @param r Room.
	 */
	public void leave(int r) {
		if (r < 0 || r >= numberOfRooms)
			throw new IllegalArgumentException();

		changeState(s -> s.onLeave(r));
	}

	/**
	 * State change handling.
	 * 
	 * @param f State transition.
	 * @return Old state before transition.
	 */
	private State changeState(UnaryOperator<State> f) {
		State oldS, newS;
		while (true) {
			oldS = currentState.get();
			newS = f.apply(oldS);
			if (oldS != newS && currentState.compareAndSet(oldS, newS)) {
				break;
			}
			if (useBackoff) {
				Backoff.delay();
			}
		}
		if (useBackoff) {
			Backoff.reset();
		}
		return oldS;
	}

	/* State class */
	private static class State {
		static final State FREE = new State(-1, 0, 0);
		final int count;
		final int room;

		State(int room, int count, int entering) {
			this.room = room;
			this.count = count;
		}

		State onEnter(int r) {
			State result;
			if (this == FREE) {
				result = new State(r, 1, 0);
			} else if (room == r) {
				result = new State(r, count + 1, 0);
			} else {
				result = this;
			}
			return result;
		}

		State onLeave(int r) {
			if (room != r) {
				throw new IllegalStateException();
			}
			return count == 1 ? FREE : new State(r, count - 1, 0);
		}

		@Override
		public String toString() {
			return room + "/" + count;
		}
	}

}
